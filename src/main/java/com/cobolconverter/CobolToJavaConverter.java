package com.cobolconverter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class CobolToJavaConverter {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java CobolToJavaConverter <cobol-source-dir> <java-output-dir>");
            System.exit(1);
        }
        
        String cobolSourceDir = args[0];
        String javaOutputDir = args[1];
        
        try {
            convertCobolToJava(cobolSourceDir, javaOutputDir);
            System.out.println("✓ Conversion completed successfully!");
        } catch (IOException e) {
            System.err.println("✗ Error during conversion: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static void convertCobolToJava(String cobolSourceDir, String javaOutputDir) throws IOException {
        File sourceDir = new File(cobolSourceDir);
        File outputDir = new File(javaOutputDir);
        
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        File[] cobolFiles = sourceDir.listFiles((dir, name) -> 
            name.endsWith(".cob") || name.endsWith(".cobol"));
        
        if (cobolFiles == null || cobolFiles.length == 0) {
            System.out.println("No COBOL files found in " + cobolSourceDir);
            return;
        }
        
        for (File cobolFile : cobolFiles) {
            String javaCode = convertFile(cobolFile);
            String javaFileName = cobolFile.getName()
                .replaceAll("\\.(cob|cobol)$", ".java");
            File javaFile = new File(outputDir, javaFileName);
            
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(javaCode);
                System.out.println("✓ Converted: " + cobolFile.getName() 
                    + " -> " + javaFileName);
            }
        }
    }
    
    private static String convertFile(File cobolFile) throws IOException {
        StringBuilder javaCode = new StringBuilder();
        String className = getClassName(cobolFile.getName());
        
        javaCode.append("// Auto-generated from COBOL\n");
        javaCode.append("// Source: ").append(cobolFile.getName()).append("\n\n");
        javaCode.append("public class ").append(className).append(" {\n");
        javaCode.append("    public static void main(String[] args) {\n");
        
        List<String> lines = Files.readAllLines(cobolFile.toPath());
        Map<String, String> variables = new HashMap<>();
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            if (trimmed.isEmpty() || trimmed.startsWith("*")) {
                continue;
            }
            
            if (trimmed.contains("PIC")) {
                String varDecl = convertVariableDeclaration(trimmed);
                if (!varDecl.isEmpty()) {
                    javaCode.append("        ").append(varDecl).append("\n");
                    extractVariable(trimmed, variables);
                }
            } else if (trimmed.startsWith("COMPUTE")) {
                javaCode.append("        ").append(convertCompute(trimmed)).append("\n");
            } else if (trimmed.startsWith("MOVE")) {
                javaCode.append("        ").append(convertMove(trimmed)).append("\n");
            } else if (trimmed.startsWith("DISPLAY")) {
                javaCode.append("        ").append(convertDisplay(trimmed)).append("\n");
            }
        }
        
        javaCode.append("    }\n");
        javaCode.append("}\n");
        
        return javaCode.toString();
    }
    
    private static String convertVariableDeclaration(String line) {
        if (line.contains("PIC 9")) {
            return "int variable;";
        } else if (line.contains("PIC X")) {
            return "String variable;";
        }
        return "";
    }
    
    private static String convertCompute(String line) {
        return "// " + line;
    }
    
    private static String convertMove(String line) {
        return "// " + line;
    }
    
    private static String convertDisplay(String line) {
        String content = line.replace("DISPLAY", "").trim();
        return "System.out.println(\"" + content + "\");";
    }
    
    private static void extractVariable(String line, Map<String, String> variables) {
        Pattern pattern = Pattern.compile("(\\d+\\s+)(\\w+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String varName = matcher.group(2);
            variables.put(varName, "int");
        }
    }
    
    private static String getClassName(String fileName) {
        return fileName.replaceAll("\\.(cob|cobol)$", "")
            .replaceAll("-", "_")
            .replaceAll("^[0-9]", "");
    }
}
