package tcs.system.lib_common.fileUtil;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;
import tcs.system.lib_common.exception.ApiExceptionStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class FileManagerUtil {
    private String directory;
    private Path basePath;
    public FileManagerUtil(String directory){
        this.directory = directory;
        this.basePath = Path.of(System.getenv("BASE_PATH"));
    }

    public String saveFile(MultipartFile file) {
        try{
            Path pathUserProfile = baseDirectory();
            // Get the original file name
            String originalFileName = file.getOriginalFilename();
            // Extract the extension
            String extension = getFileExtension(originalFileName);

            // Create a new file name with the same extension
            String newFileName =
                    UUID.randomUUID() + (extension.isEmpty() ? ".png" : "." + extension);
            // Define the path for the uploaded file
            Path filePath = pathUserProfile.resolve(newFileName);
            file.transferTo(filePath.toFile());
            return newFileName;
        } catch (IOException e) {
            throw new ApiExceptionStatusException("unable to save this file",400);
        }
    }

    public String saveFileTypeFile (File file) {
        try{

            Path pathUserProfile = baseDirectory();
            // Get the original file name
            String originalFileName = file.getName();
            // Extract the extension
            String extension = getFileExtension(originalFileName);
            String newFileName =
                    UUID.randomUUID() + (extension.isEmpty() ? ".png" : "." + extension);
            // Define the path for the uploaded file
            Path filePath = pathUserProfile.resolve(newFileName);
            Files.copy(file.toPath(),filePath);
            return newFileName;
        }catch (IOException e) {
            throw new ApiExceptionStatusException("unable to save this file : " + file.getName(),400);
        }
    }
    private Path baseDirectory(){
        try{
            Path pathDirectoryBase = Path.of(basePath.toString(), "sarana");
            File fileDirectory = new File(pathDirectoryBase.toString());
            if (!fileDirectory.exists()) {
                Files.createDirectory(pathDirectoryBase.toAbsolutePath());
            }
            Path pathUserProfile = Path.of(pathDirectoryBase.toString(), this.directory);
            File filePathUser = new File(pathUserProfile.toString());
            if (!filePathUser.exists()) {
                Files.createDirectory(pathUserProfile.toAbsolutePath());
            }
            return pathUserProfile;
        }catch (IOException e){
            throw new ApiExceptionStatusException("unable to create base directory",400);
        }
    }
    public File viewFile (String imageId){
        Path pathUserProfile = baseDirectory();
        Path pathFile = Path.of(pathUserProfile.toString(),imageId);
        if (Files.notExists(pathFile)){
            throw new ApiExceptionStatusException("There is no file name : " + imageId,400);
        }
        return  pathFile.toFile();
    }
    public DocumentContent viewImage (String imageId){
            try{
                var fileManager = viewFile(imageId);
                var resource = new FileSystemResource(fileManager);
                var responseRequest = new DocumentContent(resource, Files.probeContentType(fileManager.toPath()));
                if (Objects.isNull(responseRequest.getContentType())){
                    throw new ApiExceptionStatusException("unable to find image id: " + imageId,400);
                }
                return responseRequest;
            } catch (IOException e) {
                throw new ApiExceptionStatusException("process error Image Id:" + imageId , 400);
            }
    }
    public void deleteFile (String imageId) {
        try{
            Path pathUserProfile = baseDirectory();
            Path pathFile = Path.of(pathUserProfile.toString(),imageId);
            Files.deleteIfExists(pathFile);
        }catch (IOException e){
            throw new ApiExceptionStatusException("unable to delete file Id: " + imageId,400);
        }
    }
    public String[] getAllFilesInDirectory (){
        Path pathUserProfile = baseDirectory();
        File filePathUser = new File(pathUserProfile.toString());
        return filePathUser.list();
    }
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ""; // No extension found
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
