package cbro;

import lombok.Getter;
import org.json.simple.JSONArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ReadPart {
    public static final String FILE_PATH = "D:\\Source\\NRO TABI\\data\\girlkun\\update_data\\part";
    public static final String OUTPUT_FILE_PATH = "D:\\Source\\NRO TABI\\data\\girlkun\\update_data\\part.sql";

    public static void main(final String[] args) {
        final List<Part> parts = readParts();
        final String sql = createSql(parts);
        writeSqlToFile(sql);
        System.out.println("Done!");
    }

    private static List<Part> readParts() {
        final List<Part> parts = new ArrayList<>();
        try (final DataInputStream dis = new DataInputStream(Files.newInputStream(Paths.get(ReadPart.FILE_PATH)))) {
            for (int num = dis.readShort(), i = 0; i < num; ++i) {
                final int type = dis.readByte();
                int n = 0;
                if (type == 0) {
                    n = 3;
                } else if (type == 1) {
                    n = 17;
                } else if (type == 2) {
                    n = 14;
                } else if (type == 3) {
                    n = 2;
                }
                final JSONArray pis = new JSONArray();
                for (int k = 0; k < n; ++k) {
                    final JSONArray pi = new JSONArray();
                    pi.add(dis.readShort());
                    pi.add(dis.readByte());
                    pi.add(dis.readByte());
                    pis.add(pi);
                }
                parts.add(new Part(i, type, pis));
                System.out.println("ID Part " + i + " Type " + type + ", '" + pis.toJSONString() + "');");
            }
        } catch (IOException e) {
            System.out.println("aaa");
        }
        return parts;
    }

    private static String createSql(final List<Part> parts) {
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("USE part;\n\n");
        sqlBuilder.append("DROP TABLE IF EXISTS part;\n");
        sqlBuilder.append("CREATE TABLE part (\n");
        sqlBuilder.append("  id INT NOT NULL,\n");
        sqlBuilder.append("  type INT NOT NULL,\n");
        sqlBuilder.append("  DATA JSON NOT NULL,\n");
        sqlBuilder.append("  PRIMARY KEY (id)\n");
        sqlBuilder.append(");\n\n");
        for (final Part part : parts) {
            sqlBuilder.append("INSERT INTO part (id, type, DATA) VALUES (");
            sqlBuilder.append(part.getId()).append(", ");
            sqlBuilder.append(part.getType()).append(", ");
            sqlBuilder.append("'").append(part.getPi().toJSONString()).append("'");
            sqlBuilder.append(");\n");
        }
        return sqlBuilder.toString();
    }

    private static void writeSqlToFile(final String sql) {
        try (final Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(ReadPart.OUTPUT_FILE_PATH)), StandardCharsets.UTF_8))) {
            writer.write(sql);
        } catch (IOException e) {
            System.out.println("bbb");
        }
    }

    @Getter
    private static class Part {
        private final int id;
        private final int type;
        private final JSONArray pi;

        public Part(final int id, final int type, final JSONArray pi) {
            this.id = id;
            this.type = type;
            this.pi = pi;
        }
    }
}