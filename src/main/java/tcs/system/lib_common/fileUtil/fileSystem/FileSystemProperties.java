package tcs.system.lib_common.fileUtil.fileSystem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-system-properties")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileSystemProperties {
    private String baseDriverPath;
    private String baseDirectoryFolder;
}
