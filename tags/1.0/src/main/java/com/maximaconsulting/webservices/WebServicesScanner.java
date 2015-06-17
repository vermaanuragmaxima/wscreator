package com.maximaconsulting.webservices;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jws.WebService;

/**
 * This class scans code for the interfaces annotated with {@link WebService}
 * annotation. Actual scanning happens in the first call, a cached copy will be
 * returned afterwards.
 * 
 * @author anurag.verma
 */
public class WebServicesScanner {
	private static List<String> scanPackages;
	private static Set<Class<?>> webServices;

	public static String getServiceName(Class<?> service) {
		String serviceName = service.getAnnotation(WebService.class).serviceName();
		if (serviceName == null || serviceName.isEmpty()) {
			serviceName = service.getSimpleName();
		}
		return serviceName;
	}

	/**
	 * @return {@link Set} of interfaces annotated with {@link WebService}
	 *         annotation.
	 */
	public static Set<Class<?>> getWebServices() {
		if (webServices == null) {
			WebServicesScanner scanner = new WebServicesScanner();
			scanner.scan();
			validateServiceNames(webServices);
		}
		return webServices;
	}

	/**
	 * Initializer for the class.
	 * 
	 * @param scanPackages
	 *            list of packages to be scanned for {@link WebService}
	 *            annotated classes.
	 */
	public static void init(List<String> scanPackages) {
		WebServicesScanner.scanPackages = scanPackages;
	}

	/**
	 * Validates Service Names for Uniqueness.
	 * 
	 * @param services
	 */
	private static void validateServiceNames(Set<Class<?>> services) {
		Set<String> names = new HashSet<String>();
		Set<String> duplicates = new HashSet<String>();
		for (Class<?> service : services) {
			final String serviceName = WebServicesScanner.getServiceName(service);
			if (!names.add(serviceName)) {
				duplicates.add(serviceName);
			}
		}
		if (names.size() != services.size()) {
			throw new RuntimeException("Duplicate Service Names Found : " + duplicates);
		}
	}

	/**
	 * adds the class with specified name as a WebService to its list
	 * 
	 * @param className
	 */
	private void addToList(String className) {
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
			if (clazz.getAnnotation(WebService.class) != null && clazz.isInterface()) {
				webServices.add(clazz);
			}
		}
		catch (ClassNotFoundException e) {}
	}

	/**
	 * Actual scanning process goes here for all jars & .class files.
	 */
	private void scan() {
		webServices = new HashSet<Class<?>>();
		ClassLoader classLoader = getClass().getClassLoader();
		for (String packageName : scanPackages) {
			if (packageName == null || packageName.equals("")) {
				continue;
			}
			String path = packageName.replace('.', '/');
			Enumeration<URL> resources;
			try {
				resources = classLoader.getResources(path);
				while (resources.hasMoreElements()) {
					URL resource = resources.nextElement();
					if (resource.getProtocol().equals("jar"))
						scanJarClasses(resource);
					else
						scanFileSystemClasses(new File(resource.getFile()), packageName);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * scans .class files placed directly on disk for web-services
	 * 
	 * @param directory
	 * @param packageName
	 */
	private void scanFileSystemClasses(File directory, String packageName) {
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory())
				scanFileSystemClasses(file, packageName + "." + file.getName());
			else {
				String classExtension = ".class";
				if (file.getName().endsWith(classExtension)) {
					int classNameLength = file.getName().length() - classExtension.length();
					addToList(packageName + '.' + file.getName().substring(0, classNameLength));
				}
			}
		}
	}

	/**
	 * scans jar classes for web-services
	 * 
	 * @param resource
	 * @throws IOException
	 */
	private void scanJarClasses(URL resource) throws IOException {
		String[] names = resource.getFile().substring("file:".length()).split("!/");
		JarFile jarFile = new JarFile(new File(names[0]));
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			String entry = entries.nextElement().getName();
			if (entry.startsWith(names[1]) && entry.endsWith(".class"))
				addToList(entry.substring(0, (entry.length() - ".class".length())).replace('/', '.'));
		}
	}
}
