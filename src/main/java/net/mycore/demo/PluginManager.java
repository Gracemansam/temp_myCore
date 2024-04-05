package net.mycore.demo;


import net.mycore.demo.Plugin;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PluginManager {

    public PluginManager() {
    }


    private Map<String, Plugin> plugins = new HashMap<>();

    public PluginInstallResult installPlugin(MultipartFile file) {
        PluginInstallResult result = new PluginInstallResult();
        try {
            File file1 = convertMultipartFileToFile(file);
            String directoryPath = "path/to/extract/directory";
           List<File> exFiles = extractJarFile(file1, directoryPath);
            System.out.println("done extracting the jar file" + exFiles.toString() )  ;
            MutablePair<String, String> groupIdAndArtifactId = extractPomFromJar(file1);
            String groupId = groupIdAndArtifactId.getKey();
            String artifactId = groupIdAndArtifactId.getValue();
            try (JarFile jar = new JarFile(file1)) {
                String pomPath = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml";
                JarEntry pomEntry = jar.getJarEntry(pomPath);
                if (pomEntry != null) {
                    try (InputStream pomInputStream = jar.getInputStream(pomEntry)) {
                        File pomFile = File.createTempFile("pom", ".xml");
                        Files.copy(pomInputStream, pomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        List<String> dependencies = extractDependenciesFromPom(pomFile);
                        compileCodeWithDependencies(directoryPath, dependencies);
                        Module module = new Module();
                        module.setBasePackage(extractBasePackageName(file1));
                        System.out.println("done extracting the base Package");
                        module.setDependencies(dependencies);
                        System.out.println("done setting the dependencies");
                        URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(directoryPath).toURI().toURL()});
                        File dir = new File(directoryPath);
                        System.out.println("in the file path");
                        File[] classFiles = dir.listFiles((d, name) -> name.endsWith(".class"));
                        System.out.println("there are the class with .class" + classFiles.toString()   );
                        if (classFiles == null || classFiles.length == 0) {
                            throw new FileNotFoundException("No .class files found in the directory");
                        }
                        for (File exFile : exFiles) {
                            String className = exFile.getName().replace(".class", "");
                            Class<?> clazz = classLoader.loadClass(module.getBasePackage() + "." + className);
                            try {
                                clazz.getDeclaredMethod("main", String[].class);
                                if (!PluginInterface.class.isAssignableFrom(clazz)) {
                                    throw new IllegalArgumentException("Main class does not implement PluginInterface");
                                }
                                PluginInterface pluginInstance = (PluginInterface) clazz.getDeclaredConstructor().newInstance();
                                module.setPluginInstance(pluginInstance);
                                module.getPluginInstance().start();
                                result.setSuccess(true);
                                result.setMessage("Plugin installed successfully");
                                result.setPluginInstance(pluginInstance);
                                break;
                            } catch (NoSuchMethodException e) {
                                // This class doesn't have a main method, ignore it
                            }
                        }
                    }
                } else {
                    throw new FileNotFoundException("pom.xml not found in the JAR file");
                }
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Failed to install plugin: " + e.getMessage());
        }
        return result;
    }
//public PluginInstallResult installPlugin(MultipartFile file) {
//    PluginInstallResult result = new PluginInstallResult();
//    try {
//        File file1 = convertMultipartFileToFile(file);
//        String directoryPath = "path/to/extract/directory";
//        List<File> exFiles = extractJarFile(file1, directoryPath);
//        System.out.println("done extracting the jar file" + exFiles.toString());
//        Pair<String, String> groupIdAndArtifactId = extractPomFromJar(file1);
//        processPomFile(file1, groupIdAndArtifactId, directoryPath, exFiles, result);
//    } catch (IOException e) {
//        result.setSuccess(false);
//        result.setMessage("Failed to install plugin: " + e.getMessage());
//    }
//    return result;
//}
//
//    private void processPomFile(File file1, Pair<String, String> groupIdAndArtifactId, String directoryPath, List<File> exFiles, PluginInstallResult result) throws IOException {
//        String groupId = groupIdAndArtifactId.getKey();
//        String artifactId = groupIdAndArtifactId.getValue();
//        try (JarFile jar = new JarFile(file1)) {
//            String pomPath = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml";
//            JarEntry pomEntry = jar.getJarEntry(pomPath);
//            if (pomEntry != null) {
//                processPomEntry(jar, pomEntry, directoryPath, exFiles, result);
//            } else {
//                throw new FileNotFoundException("pom.xml not found in the JAR file");
//            }
//        }
//    }
//
//    private void processPomEntry(JarFile jar, JarEntry pomEntry, String directoryPath, List<File> exFiles, PluginInstallResult result) throws IOException {
//        try (InputStream pomInputStream = jar.getInputStream(pomEntry)) {
//            File pomFile = File.createTempFile("pom", ".xml");
//            Files.copy(pomInputStream, pomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//            List<String> dependencies = extractDependenciesFromPom(pomFile);
//            compileCodeWithDependencies(directoryPath, dependencies);
//            Module module = new Module();
//            module.setBasePackage(extractBasePackageName(pomFile));
//            System.out.println("done extracting the base Package");
//            module.setDependencies(dependencies);
//            System.out.println("done setting the dependencies");
//            processExtractedFiles(directoryPath, exFiles, module, result);
//        }
//    }
//
//    private void processExtractedFiles(String directoryPath, List<File> exFiles, Module module, PluginInstallResult result) {
//        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(directoryPath).toURI().toURL()})) {
//            for (File exFile : exFiles) {
//                if (exFile.getName().endsWith(".class")) {
//                    processClassFile(classLoader, module, exFile, result);
//                }
//            }
//        } catch (IOException e) {
//            result.setSuccess(false);
//            result.setMessage("Failed to process extracted files: " + e.getMessage());
//        }
//    }
//
//    private void processClassFile(URLClassLoader classLoader, Module module, File exFile, PluginInstallResult result) {
//        try {
//            String className = exFile.getName().replace(".class", "");
//            Class<?> clazz = classLoader.loadClass(module.getBasePackage() + "." + className);
//            Method mainMethod = null;
//            try {
//                mainMethod = clazz.getDeclaredMethod("main", String[].class);
//            } catch (NoSuchMethodException e) {
//                // This class doesn't have a main method, ignore it
//            }
//            if (mainMethod != null && PluginInterface.class.isAssignableFrom(clazz)) {
//                PluginInterface pluginInstance = (PluginInterface) clazz.getDeclaredConstructor().newInstance();
//                module.setPluginInstance(pluginInstance);
//                module.getPluginInstance().start();
//                result.setSuccess(true);
//                result.setMessage("Plugin installed successfully");
//                result.setPluginInstance(pluginInstance);
//            }
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
//            result.setSuccess(false);
//            result.setMessage("Failed to process class file: " + e.getMessage());
//        } catch (NoSuchMethodException| RuntimeException e) {
//           e.printStackTrace();
//        }
//    }

    public void uninstallPlugin(String pluginId) {
        // Code to unload the module and remove it from the plugins map
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }
//    private void loadModuleFromJarFile(MultipartFile multipartFile,Module module) {
//        try {
//            // Convert MultipartFile to File
//            File file = convertMultipartFileToFile(multipartFile);
//
//            // Extract the jar file to a directory
//            String directoryPath = "path/to/extract/directory";
//            extractJarFile(file, directoryPath);
//            // Extract groupId and artifactId from the pom.xml file in the jar
//            MutablePair<String, String> groupIdAndArtifactId = extractPomFromJar(file);
//            String groupId = groupIdAndArtifactId.getKey();
//            String artifactId = groupIdAndArtifactId.getValue();
//            try (JarFile jar = new JarFile(file)) {
//                // Construct the path to the pom.xml file in the jar using the groupId and artifactId
//                String pomPath = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml";
//                JarEntry pomEntry = jar.getJarEntry(pomPath);
//
//                // If the pom.xml entry is not null, extract it
//                if (pomEntry != null) {
//                    try (InputStream pomInputStream = jar.getInputStream(pomEntry)) {
//                        // Create a temporary File for the pom.xml
//                        File pomFile = File.createTempFile("pom", ".xml");
//                        Files.copy(pomInputStream, pomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//
//                        // After extracting the pom.xml, call the extractDependenciesFromPom method
//                        List<String> dependencies = extractDependenciesFromPom(pomFile);
//                        // Continue with the rest of your code...
//
//                        // Compile the code in the directory with the dependencies
//                        compileCodeWithDependencies(directoryPath, dependencies);
//
//                        // Load the module from the JAR file
//                        //module = new Module();
//
//                        // Save the necessary information in the Module entity
//                        module.setBasePackage(extractBasePackageName(file));
//                        //  module.setName(plugin.getName());
//                        module.setDependencies(dependencies);
//                        module.setName("f");
//                        module.setDescription("f");
//                        module.setVersion("f");
//
//                    }
//                } else {
//                    throw new FileNotFoundException("pom.xml not found in the JAR file");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            // Extract the dependencies from the jar file's manifest file
//        //    List<String> dependencies = extractDependenciesFromPom(file);
//
//            // Download the dependencies using Maven
//          //  downloadDependencies(dependencies);
//
//
//            // Add other necessary information here
//            URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(directoryPath).toURI().toURL()});
//
//            // Get all the .class files in the directory
//            File dir = new File(directoryPath);
//            File[] classFiles = dir.listFiles((d, name) -> name.endsWith(".class"));
//
//            // Iterate over the .class files
//            for (File classFile : classFiles) {
//                // Get the class name
//                String className = classFile.getName().replace(".class", "");
//
//                // Load the class
//                Class<?> clazz = classLoader.loadClass(module.getBasePackage() + "." + className);
//
//                // Check if the class has a main method
//                try {
//                    clazz.getDeclaredMethod("main", String[].class);
//
//
//                    // If the class has a main method, it's the main class
//                    String mainClassName = clazz.getName();
//                    System.out.println("Main class: " + mainClassName);
//                    // Check if the main class implements PluginInterface
//                    if (!PluginInterface.class.isAssignableFrom(clazz)) {
//                        throw new IllegalArgumentException("Main class does not implement PluginInterface");
//                    }
//
//                    // Create an instance of the main class
//                    PluginInterface pluginInstance = (PluginInterface) clazz.getDeclaredConstructor().newInstance();
//
//                    // Add the plugin instance to the module
//                    module.setPluginInstance(pluginInstance);
//
//                    break;
//                } catch (NoSuchMethodException e) {
//                    // This class doesn't have a main method, ignore it
//                }
//            }
//
//            // Save the module entity
//         //   saveModule(module);
//        } catch (IOException | ClassNotFoundException | InvocationTargetException |InstantiationException
//                | IllegalAccessException e) {
//            e.printStackTrace();
//
//        }
//    }

    private String extractBasePackageName(File file) throws IOException {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    return className.substring(0, className.lastIndexOf('.'));
                }
            }
        }
        return "";
    }

    private void saveModule(Module module) {
        // Code to save the module entity
    }


//    public void extractJarFile(File file, String directoryPath) throws IOException {
//        JarFile jarFile = new JarFile(file);
//        Enumeration<JarEntry> entries = jarFile.entries();
//        while (entries.hasMoreElements()) {
//            JarEntry entry = entries.nextElement();
//            File entryDestination = new File(directoryPath, entry.getName());
//            if (entry.isDirectory()) {
//                entryDestination.mkdirs();
//            } else {
//                entryDestination.getParentFile().mkdirs();
//                try (InputStream in = jarFile.getInputStream(entry);
//                     OutputStream out = new FileOutputStream(entryDestination)) {
//                    byte[] buffer = new byte[1024];
//                    int length;
//                    while ((length = in.read(buffer)) != -1) {
//                        out.write(buffer, 0, length);
//                    }
//                }
//            }
//        }
//    }
public List<File> extractJarFile(File file, String directoryPath) throws IOException {
    List<File> extractedFiles = new ArrayList<>();
    JarFile jarFile = new JarFile(file);
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        File entryDestination = new File(directoryPath, entry.getName());
        if (entry.isDirectory()) {
            entryDestination.mkdirs();
        } else {
            entryDestination.getParentFile().mkdirs();
            try (InputStream in = jarFile.getInputStream(entry);
                 OutputStream out = new FileOutputStream(entryDestination)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
            }
            extractedFiles.add(entryDestination);
        }
    }
    return extractedFiles;
}
//    public List<String> extractDependenciesFromManifest(File file) throws IOException {
//        JarFile jarFile = new JarFile(file);
//        String dependencies = jarFile.getManifest().getMainAttributes().getValue("Dependencies");
//        return dependencies != null ? Arrays.asList(dependencies.split(",")) : new ArrayList<>();
//    }
//
//    public void downloadDependencies(List<String> dependencies) throws IOException {
//        String pomTemplate = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
//                "  <modelVersion>4.0.0</modelVersion>\n" +
//                "  <groupId>temp</groupId>\n" +
//                "  <artifactId>temp</artifactId>\n" +
//                "  <version>1.0</version>\n" +
//                "  <dependencies>\n" +
//                "%s\n" +
//                "  </dependencies>\n" +
//                "</project>";
//        String dependencyTemplate = "    <dependency>\n" +
//                "      <groupId>%s</groupId>\n" +
//                "      <artifactId>%s</artifactId>\n" +
//                "      <version>%s</version>\n" +
//                "    </dependency>";
//        StringBuilder dependenciesXml = new StringBuilder();
//        for (String dependency : dependencies) {
//            String[] parts = dependency.split(":");
//            dependenciesXml.append(String.format(dependencyTemplate, parts[0], parts[1], parts[2]));
//        }
//        String pomXml = String.format(pomTemplate, dependenciesXml.toString());
//        Path pomPath = Files.createTempFile("pom", ".xml");
//        Files.write(pomPath, pomXml.getBytes());
//        try {
//            Process process = new ProcessBuilder("mvn", "dependency:copy-dependencies", "-f", pomPath.toString()).start();
//            process.waitFor();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    public void compileCodeWithDependencies(String directoryPath, List<String> dependencies) {
        String classpath = String.join(File.pathSeparator, dependencies);
        try {
            Process process = new ProcessBuilder("javac", "-cp", classpath, directoryPath + "/*.java").start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public List<String> extractDependenciesFromPom(File pomFile) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        try {
            model = reader.read(new FileReader(pomFile));
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return model.getDependencies().stream()
                .map(dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
                .collect(Collectors.toList());
    }

    public MutablePair<String, String> extractPomFromJar(File jarFile) throws IOException {
        // Open the JAR file
        try (JarFile jar = new JarFile(jarFile)) {
            // Get the entries
            Enumeration<JarEntry> entries = jar.entries();

            // Iterate over the entries
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                // If the entry is a pom.xml file, extract it
                if (entry.getName().endsWith("pom.xml")) {
                    try (InputStream pomInputStream = jar.getInputStream(entry)) {
                        // Create a temporary File for the pom.xml
                        File pomFile = File.createTempFile("pom", ".xml");
                        Files.copy(pomInputStream, pomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        // Parse the pom.xml file to get the groupId and artifactId
                        MavenXpp3Reader reader = new MavenXpp3Reader();
                        Model model = reader.read(new FileReader(pomFile));
                        String groupId = model.getGroupId();
                        String artifactId = model.getArtifactId();

                        // Return the groupId and artifactId
                        return new MutablePair<>(groupId, artifactId);
                    } catch (Exception e) {
                        throw new IOException("Failed to parse pom.xml", e);
                    }
                }
            }

            throw new FileNotFoundException("pom.xml not found in the JAR file");
        }
    }
}