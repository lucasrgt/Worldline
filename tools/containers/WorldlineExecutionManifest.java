import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Strict verifier for the content-addressed native execution output manifest. */
final class WorldlineExecutionManifest {
    private static final List<String> ROOT_FIELDS = List.of("schema", "files");
    private static final List<String> FILE_FIELDS = List.of("name", "sha256", "size");

    private WorldlineExecutionManifest() { }

    static String verify(WorldlineExecutionPlan plan) throws Exception {
        Path path = plan.outputs().manifest();
        byte[] bytes = Files.readAllBytes(path);
        Map<String, Object> root = ExecutionJsonParser.object(new String(bytes, StandardCharsets.UTF_8));
        ExecutionJson.exactKeys(root, ROOT_FIELDS, "execution output manifest");
        require("worldline.execution-output-manifest.v1".equals(root.get("schema")), "manifest schema drift");
        List<Map<String, Object>> files = ExecutionJsonParser.objects(root.get("files"));
        require(files.size() == 3, "manifest file count drift");
        List<String> names = List.of("stdout", "stderr", "metrics");
        List<Path> paths = List.of(plan.outputs().stdout(), plan.outputs().stderr(), plan.outputs().metrics());
        StringBuilder json = new StringBuilder("{\"schema\":\"worldline.execution-output-manifest.v1\",\"files\":[");
        for (int index = 0; index < files.size(); index++) {
            Map<String, Object> file = files.get(index); Path output = paths.get(index);
            ExecutionJson.exactKeys(file, FILE_FIELDS, "execution output entry");
            require(names.get(index).equals(file.get("name")), "manifest output order drift");
            require(ExecutionJson.sha256(output).equals(file.get("sha256")), "manifest output digest drift");
            require(Long.valueOf(Files.size(output)).equals(file.get("size")), "manifest output size drift");
            if (index > 0) json.append(',');
            json.append("{\"name\":").append(ExecutionJson.quote(names.get(index)))
                    .append(",\"sha256\":").append(ExecutionJson.quote((String) file.get("sha256")))
                    .append(",\"size\":").append(file.get("size")).append('}');
        }
        json.append("]}");
        require(java.util.Arrays.equals(bytes, ExecutionJson.bytes(json.toString())),
                "manifest is not canonically encoded");
        return ExecutionJson.sha256(bytes);
    }

    private static void require(boolean value, String message) { ExecutionJson.require(value, message); }
}
