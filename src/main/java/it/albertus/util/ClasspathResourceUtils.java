package it.albertus.util;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * List resources available from the classpath.
 * 
 * @see <a href="http://stackoverflow.com/a/3923182/3260495">Get a list of
 *      resources from classpath directory - Stack Overflow</a>
 */
public class ClasspathResourceUtils {

	private ClasspathResourceUtils() {
		throw new IllegalAccessError("Utility class");
	}

	/**
	 * For all elements of java.class.path get a Collection of resources Pattern
	 * pattern = Pattern.compile(".*"); gets all resources.
	 * 
	 * @param pattern the pattern to match.
	 * @return the resources in the order they are found.
	 */
	public static List<Resource> getResourceList(final Pattern pattern) {
		final List<Resource> retval = new ArrayList<Resource>();
		final String classPath = System.getProperty("java.class.path", ".");
		final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
		for (final String element : classPathElements) {
			retval.addAll(getResources(element, pattern));
		}
		return retval;
	}

	private static List<Resource> getResources(final String element, final Pattern pattern) {
		final List<Resource> retval = new ArrayList<Resource>();
		final File file = new File(element);
		if (file.isDirectory()) {
			retval.addAll(getResourcesFromDirectory(file, pattern));
		}
		else {
			retval.addAll(getResourcesFromJarFile(file, pattern));
		}
		return retval;
	}

	private static List<Resource> getResourcesFromJarFile(final File file, final Pattern pattern) {
		final List<Resource> retval = new ArrayList<Resource>();
		ZipFile zf;
		try {
			zf = new ZipFile(file);
		}
		catch (final IOException e) {
			throw new IOError(e);
		}
		final Enumeration<? extends ZipEntry> e = zf.entries();
		while (e.hasMoreElements()) {
			final ZipEntry ze = e.nextElement();
			final String fileName = ze.getName();
			final boolean accept = pattern.matcher(fileName).matches();
			if (accept && !ze.isDirectory()) {
				retval.add(new Resource(fileName, ze.getSize(), ze.getTime()));
			}
		}
		try {
			zf.close();
		}
		catch (final IOException ex) {
			throw new IOError(ex);
		}
		return retval;
	}

	private static List<Resource> getResourcesFromDirectory(final File directory, final Pattern pattern) {
		final List<Resource> retval = new ArrayList<Resource>();
		final File[] fileList = directory.listFiles();
		for (final File file : fileList) {
			if (file.isDirectory()) {
				retval.addAll(getResourcesFromDirectory(file, pattern));
			}
			else {
				final String fileName = file.getPath();
				final boolean accept = pattern.matcher(fileName).matches();
				if (accept) {
					retval.add(new Resource(fileName, file.length(), file.lastModified()));
				}
			}
		}
		return retval;
	}

	/**
	 * List the resources that match args[0].
	 * 
	 * @param args args[0] is the pattern to match, or list all resources if
	 *        there are no args.
	 */
	public static void main(final String[] args) {
		final Pattern pattern;
		if (args.length < 1) {
			pattern = Pattern.compile(".*");
		}
		else {
			pattern = Pattern.compile(args[0]);
		}
		final Collection<Resource> list = ClasspathResourceUtils.getResourceList(pattern);
		for (final Resource name : list) {
			System.out.println(name);
		}
	}

}
