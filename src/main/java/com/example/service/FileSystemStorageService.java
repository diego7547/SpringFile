package com.example.service;


import com.example.exception.MediaFileNotFoundException;
import com.example.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileSystemStorageService {

    private final static String STORAGE_LOCATION = "mediafile";

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(STORAGE_LOCATION));
        } catch (IOException ex) {
            throw new StorageException("No se pudo crear el almacén de archivos: " + STORAGE_LOCATION);
        }
    }

    public String store(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(originalFilename);
        if (file.isEmpty()) {
            throw new StorageException("No se puede almacenar un archivo vacío " + originalFilename);
        }
        try {
            InputStream inputStream = file.getInputStream();
            Files.copy(inputStream, Paths.get(STORAGE_LOCATION).resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new StorageException("Falló al almacenar el archivo " + originalFilename);
        }
        return filename;
    }

    public Resource loadAsResource(String filename) {
        try {
            Path path = Paths.get(STORAGE_LOCATION).resolve(filename);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new MediaFileNotFoundException("El archivo no ha sido encontrado: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new MediaFileNotFoundException("El archivo no ha sido encontrado: " + filename);
        }
    }

    public void delete(String filename) {
        Path path = Paths.get(STORAGE_LOCATION).resolve(filename);
        try {
            FileSystemUtils.deleteRecursively(path);
        } catch (IOException ex) { // lo dejamos pasar
        }
    }

}