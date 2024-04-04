package net.mycore.demo;


import net.mycore.demo.Plugin;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class PluginManager {

    public PluginManager() {
    }


    private Map<String, Plugin> plugins = new HashMap<>();

    public void installPlugin(MultipartFile file) {

        // Code to load the module from the JAR file and add it to the plugins map
        String filename = file.getOriginalFilename();
        String pluginId = filename.substring(0, filename.lastIndexOf("."));
        Plugin plugin = new Plugin();
        plugin.setId(1L);
        plugins.put(pluginId, plugin);
        plugin.setName("f");
        plugin.setDescription("f");
        plugin.setVersion("f");

        System.out.println("this is the plugin" + plugin);



        loadModuleFromJarFile(file, plugin);
        plugin.getPluginInstance().start();




    }

    public void uninstallPlugin(String pluginId) {
        // Code to unload the module and remove it from the plugins map
    }

//    private void loadModuleFromJarFile(MultipartFile multipartFile, Plugin plugin) {
//        try {
//            // Convert MultipartFile to File
//            File file = convertMultipartFileToFile(multipartFile);
//
//            // Open the JAR file
//            JarFile jarFile = new JarFile(file);
//
//            // Get the entries
//            Enumeration<JarEntry> entries = jarFile.entries();
//
//            // Create a new URLClassLoader
//            URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
//
//            // Iterate over the entries
//            while (entries.hasMoreElements()) {
//                JarEntry entry = entries.nextElement();
//
//                // If the entry is a class, load it
//                if (entry.getName().endsWith(".class")) {
//                    String className = entry.getName().replace("/", ".").replace(".class", "");
//                    Class<?> clazz = classLoader.loadClass(className);
//                    //   plugin.setPluginClass(clazz);
//
//                    // Create an instance of the class and cast it to PluginInterface
//                    PluginInterface pluginInstance = (PluginInterface) clazz.getDeclaredConstructor().newInstance();
//                    System.out.println("this is th instace "+ pluginInstance);
//
//                    // Add the instance to the plugin
//                    plugin.setPluginInstance(pluginInstance);
//
//
//                }
//            }
//
//            // Close the JAR file and the class loader
//            jarFile.close();
//            classLoader.close();
//        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
//                 IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
//

//    private void loadModuleFromJarFile(MultipartFile multipartFile, Plugin plugin) {
//        try {
//            // Convert MultipartFile to File
//            File file = convertMultipartFileToFile(multipartFile);
//
//            // Open the JAR file
//            JarFile jarFile = new JarFile(file);
//
//            // Create a new URLClassLoader with parent as null to isolate the classloading
//            URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, null);
//
//            // Iterate over the entries
//            Enumeration<JarEntry> entries = jarFile.entries();
//            while (entries.hasMoreElements()) {
//                JarEntry entry = entries.nextElement();
//
//                // If the entry is a class, load it
//                if (entry.getName().endsWith(".class")) {
//                    String className = entry.getName().replace("/", ".").replace(".class", "");
//                    Class<?> clazz = classLoader.loadClass(className);
//
//                    // Check if the loaded class implements PluginInterface
//                    if (PluginInterface.class.isAssignableFrom(clazz)) {
//                        // Create an instance of the class and cast it to PluginInterface
//                        Object pluginInstanceObj = clazz.getDeclaredConstructor().newInstance();
//                        if (pluginInstanceObj instanceof PluginInterface) {
//                            PluginInterface pluginInstance = (PluginInterface) pluginInstanceObj;
//
//                            // Add the instance to the plugin
//                            plugin.setPluginInstance(pluginInstance);
//                        } else {
//                            // Log an error if the loaded class does not implement PluginInterface
//                            System.err.println("Error: Class " + className + " does not implement PluginInterface");
//                        }
//                    } else {
//                        // Log an error if the loaded class is not assignable to PluginInterface
//                        System.err.println("Error: Class " + className + " is not assignable to PluginInterface");
//                    }
//
//                }
//            }
//
//            // Close the JAR file (Note: classLoader.close() is not called to keep the classes loaded)
//            jarFile.close();
//        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
//                 IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
private void loadModuleFromJarFile(MultipartFile multipartFile, Plugin plugin) {
    try {
        // Convert MultipartFile to File
        File file = convertMultipartFileToFile(multipartFile);

        // Load the module from the JAR file
        loadModuleFromJarFile(file, plugin);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void loadModuleFromJarFile(File file, Plugin plugin) {
        try {
            // Open the JAR file
            JarFile jarFile = new JarFile(file);

            // Get the entries
            Enumeration<JarEntry> entries = jarFile.entries();

            // Create a new URLClassLoader
            URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            System.out.println("this is the class loader" + classLoader.toString());

            // Iterate over the entries
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                // If the entry is a class, load it
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    System.out.println("this is the class name" + className);
                    Class<?> clazz = classLoader.loadClass(className);

                    // If the class is a plugin, create an instance and add it to the plugin
                    if (PluginInterface.class.isAssignableFrom(clazz)) {
                        PluginInterface pluginInstance = (PluginInterface) clazz.getDeclaredConstructor().newInstance();
                        plugin.setPluginInstance(pluginInstance);
                    }
                } else if (entry.getName().endsWith(".jar")) {
                    // If the entry is a JAR file, load it
                    InputStream jarStream = jarFile.getInputStream(entry);
                    File tempFile = File.createTempFile("nested", ".jar");
                    Files.copy(jarStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    loadModuleFromJarFile(tempFile, plugin);
                }
            }

            // Close the JAR file and the class loader
            jarFile.close();
            classLoader.close();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }
}