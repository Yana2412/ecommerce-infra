package com.example.demo.services;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioService {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access.name}")
    private String accessKey;

    @Value("${minio.access.secret}")
    private String secretKey;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public String subirImagen(MultipartFile archivo) throws Exception {
        // Inicializar el cliente de MinIO
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();

        // Crear el bucket si no existe
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        // Generar un nombre único para el archivo para evitar sobreescrituras
        String nombreArchivo = UUID.randomUUID().toString() + "-" + archivo.getOriginalFilename();

        // Subir el archivo a MinIO
        try (InputStream inputStream = archivo.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(nombreArchivo)
                            .stream(inputStream, archivo.getSize(), -1)
                            .contentType(archivo.getContentType())
                            .build()
            );
        }

        // Retornar la URL pública (o ruta) donde se guardó la imagen
        return minioUrl + "/" + bucketName + "/" + nombreArchivo;
    }
}
