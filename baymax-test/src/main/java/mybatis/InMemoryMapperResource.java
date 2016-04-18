package mybatis;

import org.springframework.security.util.InMemoryResource;

/**
 * Created by sidawei on 16/4/16.
 */
public class InMemoryMapperResource extends InMemoryResource {

    private String path;

    public InMemoryMapperResource(String path, String source) {
        super(source);
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }
}
