package wang.jinjing.editor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
class BrailleEditorAppApplicationTests {

    @Test
    void contextLoads() {
    }

    public static class TestDataGenerator {

        public static void main(String[] args) {
            StringBuilder sql = new StringBuilder();

            // 生成用户数据（50条独立插入语句）
            generateUsers(sql, 50);

            // 生成团队数据（10个团队）
            generateTeam(sql, 1, 11, "团队1", "测试团队1");
            generateTeam(sql, 2, 31, "团队2", "测试团队2");
            // 新增团队3-10，所有者均为用户11
            for (int teamId = 3; teamId <= 10; teamId++) {
                generateTeam(sql, teamId, 11, "团队" + teamId, "测试团队" + teamId);
            }

            // 生成所有团队成员关系
            generateMemberRelations(sql);

            System.out.println(sql.toString());

            // 写入文件
            try {
                Path path = Paths.get("E:\\ideaProjects\\braille-editor\\braille-editor-app\\src\\test\\resources\\test-data.sql");
                Files.write(path, sql.toString().getBytes());
                System.out.println("SQL文件已生成至：" + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("文件写入失败：");
                e.printStackTrace();
            }
        }

        // 生成用户插入语句（保持不变）
        private static void generateUsers(StringBuilder sql, int count) {
            sql.append("-- 插入系统用户\n");
            for (int i = 1; i <= count; i++) {
                String role = (i <= 10) ? "admin" : "user";
                String prefix = (i <= 10) ? "admin" : "user";
                String nickname = (i <= 10) ? "系统管理员" : "普通用户";
                String phone = String.format("%011d", 12345678900L + i);

                sql.append(buildUserInsert(i, prefix, nickname, role, phone))
                        .append("\n");
            }
            sql.append("\n");
        }

        // 构建用户插入模板（保持不变）
        private static String buildUserInsert(int id, String prefix, String nickname,
                                              String role, String phone) {
            return String.format(
                    "INSERT INTO editor_user " +
                            "(id, username, uuid, nickname, sys_role, password, phone) " +
                            "VALUES (%d, '%s%d', UUID(), '%s%d', '%s', " +
                            "'$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '%s');",
                    id, prefix, id, nickname, id, role, phone);
        }

        // 生成团队插入语句（保持不变）
        private static void generateTeam(StringBuilder sql, int teamId, int ownerId,
                                         String teamName, String description) {
            sql.append(String.format(
                    "INSERT INTO editor_team " +
                            "(id, owner, team_name, uuid, description) " +
                            "VALUES (%d, %d, '%s', UUID(), '%s');\n",
                    teamId, ownerId, teamName, description));
        }

        // 生成团队成员关系（新增团队3-10逻辑）
        private static void generateMemberRelations(StringBuilder sql) {
            sql.append("-- 插入团队成员\n");

            // 原始团队1关系
            buildMemberInsert(sql, 1, 11, "owner");
            buildRangeInserts(sql, 1, 12, 20, "admin");
            buildRangeInserts(sql, 1, 21, 30, "member");

            // 原始团队2关系
            buildMemberInsert(sql, 2, 31, "owner");
            buildRangeInserts(sql, 2, 32, 40, "admin");
            buildRangeInserts(sql, 2, 41, 50, "member");

            // 新增团队3-10关系
            for (int teamId = 3; teamId <= 10; teamId++) {
                // 设置团队所有者
                buildMemberInsert(sql, teamId, 11, "owner");
                // 添加管理员12-15
                buildRangeInserts(sql, teamId, 12, 15, "admin");
                // 添加成员16-30
                buildRangeInserts(sql, teamId, 16, 30, "member");
            }
        }

        // 构建单个成员插入（保持不变）
        private static void buildMemberInsert(StringBuilder sql, int teamId,
                                              int userId, String role) {
            sql.append(String.format(
                    "INSERT INTO editor_team_member " +
                            "(team_id, user_id, team_role) " +
                            "VALUES (%d, %d, '%s');\n",
                    teamId, userId, role));
        }

        // 构建范围成员插入（保持不变）
        private static void buildRangeInserts(StringBuilder sql, int teamId,
                                              int start, int end, String role) {
            for (int userId = start; userId <= end; userId++) {
                buildMemberInsert(sql, teamId, userId, role);
            }
        }
    }
}
