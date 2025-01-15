package com.pharmacy.intelrx.auxilliary.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@RequiredArgsConstructor
@Service
public class S3Service {
    private final AmazonS3 amazonS3;
    private final String bucketName = "intelrxdevimages";

    public S3Service() {
        this.amazonS3 = new AmazonS3Client(); // Initialize your Amazon S3 client here
    }

    public String uploadFileDoc(String base64Data,String path) throws IOException {
        // convert base64 string to a file
        byte[] fileBytes = Base64.getDecoder().decode(base64Data);
        File file = new File("tempFile");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(fileBytes);
        }

        // generate file name
        String fileName = generateFileName1(file);

        // add directory name
        String directoryName = "uploads/"+path;
        String fileKey = directoryName + fileName;

        String getExt = determineFileExtension(base64Data);

        // upload file
        PutObjectRequest request = new PutObjectRequest(bucketName, fileKey, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("plain/" + getExt /**getExtension(file)**/);
        metadata.addUserMetadata("Title", "File Upload - " + fileName);
        metadata.setContentLength(file.length());
        request.setMetadata(metadata);
        amazonS3.putObject(request);

        // delete file
        file.delete();

        return fileKey;
    }

    public String uploadFile(MultipartFile banner) throws IOException {
        // convert multipart file  to a file

        File file = new File(banner.getOriginalFilename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(banner.getBytes());
        }

        // generate file name
        String fileName = generateFileName(banner);

        // add directory name
        String directoryName = "uploads/articles/";
        String fileKey = directoryName + fileName;

        // upload file
        PutObjectRequest request = new PutObjectRequest(bucketName, fileKey, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("plain/" + FilenameUtils.getExtension(banner.getOriginalFilename()));
        metadata.addUserMetadata("Title", "File Upload - " + fileName);
        metadata.setContentLength(file.length());
        request.setMetadata(metadata);
        amazonS3.putObject(request);

        // delete file
        file.delete();

        return fileKey;
    }


    // Your other code here
    public FetchedImage fetchImage(String fileName) {
        try {
            if (bucketIsEmpty()) {
                throw new IOException("Requested bucket does not exist or is empty");
            }
            if (fileName == null) {
                return null;
            }

            // Attempt to fetch the S3Object
            S3Object object = amazonS3.getObject(bucketName, fileName);

            try (S3ObjectInputStream s3is = object.getObjectContent()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int nRead;
                while ((nRead = s3is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                byte[] imageBytes = buffer.toByteArray();
                String url = object.getObjectContent().getHttpRequest().getURI().toString();
                return new FetchedImage(url, imageBytes);
            }
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                // Return null if the image file is not found (HTTP 404 Not Found)
                return null;
            } else {
                // Handle other Amazon S3 exceptions as needed
                e.printStackTrace();
                // Optionally, throw a custom exception or log the error
                throw new RuntimeException("An error occurred while fetching the image", e);
            }
        } catch (IOException e) {
            // Handle other IOExceptions
            e.printStackTrace();
            // Optionally, throw a custom exception or log the error
            throw new RuntimeException("An error occurred while fetching the image", e);
        }
    }


    public boolean delete(String fileName) {
        File file = Paths.get(fileName).toFile();
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    private boolean bucketIsEmpty() {
        ListObjectsV2Result result = amazonS3.listObjectsV2(this.bucketName);
        if (result == null) {
            return false;
        }
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        return objects.isEmpty();
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" /", "_");
    }

    private String generateFileName1(File file) {
        // Implement your logic to generate a unique file name
        // You can use the existing logic or come up with a new one
        // For example, you can use a timestamp or UUID
        // For demonstration purposes, this example uses the original file name
        // Extract the file extension (if any)
        String fileExtension = ".jpg";
//            int dotIndex = file.getName().lastIndexOf('.');
//            if (dotIndex > 0) {
//                fileExtension = file.getName().substring(dotIndex);
//            }

        // Generate a unique file name using timestamp and UUID
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String timestamp = dateFormat.format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // Combine the original file name, timestamp, and UUID to create a unique file name
        String uniqueFileName = file.getName() + "_" + timestamp + "_" + uuid + fileExtension;

        return uniqueFileName;

    }

    private String determineFileExtension(String data) {
        String[] strings = data.split(",");
        String extension;
        switch (strings[0]) {
            // Image types
            case "data:image/jpeg;base64":
                extension = ".jpeg";
                break;
            case "data:image/jpg;base64":
                extension = ".jpg";
                break;
            case "data:image/png;base64":
                extension = ".png";
                break;
            case "data:image/gif;base64":
                extension = ".gif";
                break;
            case "data:image/bmp;base64":
                extension = ".bmp";
                break;
            case "data:image/tiff;base64":
                extension = ".tiff";
                break;
            case "data:image/webp;base64":
                extension = ".webp";
                break;
            // Document formats
            case "data:application/pdf;base64":
                extension = ".pdf";
                break;
            case "data:application/msword;base64":
                extension = ".doc";
                break;
            case "data:application/vnd.openxmlformats-officedocument.wordprocessingml.document;base64":
                extension = ".docx";
                break;
            case "data:application/vnd.ms-excel;base64":
                extension = ".xls";
                break;
            case "data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64":
                extension = ".xlsx";
                break;
            // Additional image formats
            case "data:image/svg+xml;base64":
                extension = ".svg";
                break;
            case "data:image/jp2;base64":
                extension = ".jp2";
                break;
            case "data:image/x-icon;base64":
                extension = ".ico";
                break;
            // Add more cases for other image and document types as needed
            default:
                extension = ".unknown";
                break;
        }
        return extension;
    }


    public class FetchedImage {
        private String imageUrl;
        private byte[] imageBytes;

        public FetchedImage(String imageUrl, byte[] imageBytes) {
            this.imageUrl = imageUrl;
            this.imageBytes = imageBytes;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public byte[] getImageBytes() {
            return imageBytes;
        }
    }
}
