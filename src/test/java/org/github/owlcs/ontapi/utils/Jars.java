package org.github.owlcs.ontapi.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test utilities for searching jars.
 * <p>
 * Created by @szuev on 19.02.2018.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Jars {

    private static final String[] FORBIDDEN_JAR_TYPES = new String[]{"sources", "javadoc", "original", "tests"};

    public static URI find(String artifactId) {
        return find(null, artifactId);
    }

    public static URI find(Path dir) {
        return find(dir, FORBIDDEN_JAR_TYPES);
    }

    public static URI find(String groupId, String artifactId) {
        return find(groupId, artifactId, FORBIDDEN_JAR_TYPES);
    }

    /**
     * Finds a Jar URI by maven project details.
     * The result jar-uri may not contain in class-path.
     *
     * @param groupId    String, nullable
     * @param artifactId String, not null
     * @param forbidden  String[] of jars name parts to exclude
     * @return URI, not null
     * @throws NullPointerException     - wrong input
     * @throws IllegalArgumentException - wrong input
     * @throws AssertionError           - unable to find a single jar uri corresponding the input params
     */
    public static URI find(String groupId, String artifactId, String... forbidden) {
        if (artifactId == null) throw new NullPointerException("Null artifactId");
        URLClassLoader loader = (URLClassLoader) Jars.class.getClassLoader();
        List<Path> dirs = locations(loader, groupId, artifactId, null)
                .map(Path::getParent)
                .distinct()
                .collect(Collectors.toList());
        if (dirs.size() != 1) {
            if (dirs.isEmpty())
                throw new AssertionError("No " + artifactId + "-{version}.jar found.");
            throw new AssertionError("Query: " + artifactId + "-{version}.jar. Found: " + dirs);
        }
        return find(dirs.get(0), forbidden);
    }

    /**
     * Finds a primary jar inside the specified directory.
     *
     * @param target    Path, directory to process, not null
     * @param forbidden String[] of jars name parts to exclude
     * @return URI, not null
     * @throws NullPointerException     - wrong input
     * @throws IllegalArgumentException - wrong input
     * @throws AssertionError           - unable to find a single jar uri corresponding the input params
     */
    public static URI find(Path target, String... forbidden) {
        List<URI> uris;
        try {
            uris = list(target = target.toRealPath(), forbidden);
        } catch (IOException e) {
            throw new AssertionError("Exception while traversing dir <" + target + ">", e);
        }
        if (uris.size() != 1) {
            if (uris.isEmpty())
                throw new AssertionError("No jars found in directory <" + target + ">");
            throw new AssertionError("To many results: " + uris);
        }
        return uris.get(0);
    }

    /**
     * Returns all artifacts for the given details from the specified {@code URLClassLoader}.
     *
     * @param loader     {@link URLClassLoader}, not {@code null}
     * @param groupID    String, not {@code null}
     * @param artifactID String, not {@code null}
     * @param version    String, not {@code null}
     * @return Stream of {@link Path}s
     */
    public static Stream<Path> locations(URLClassLoader loader, String groupID, String artifactID, String version) {
        String search = toStringPath(groupID, artifactID, version);
        return Arrays.stream(loader.getURLs())
                .map(s -> {
                    try {
                        return s.toURI();
                    } catch (URISyntaxException e) {
                        // ignore
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(f -> "file".equals(f.getScheme()))
                .map(Paths::get)
                .filter(p -> p.toString().contains(search));
    }

    /**
     * Converts maven dependency to path
     * Example:
     * {@code
     * &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
     * &lt;artifactId&gt;commons-csv&lt;/artifactId&gt;
     * &lt;version&gt;1.5&lt;/version&gt;
     * } => "org/apache/commons/commons-csv/1.5"
     * <p>
     * </pre>
     *
     * @param groupId    String, can be null
     * @param artifactId String, can be null
     * @param version    String, can be null
     * @return the Path or null
     */
    public static Path toPath(String groupId, String artifactId, String version) {
        if (artifactId == null) {
            if (groupId == null && version == null)
                throw new IllegalArgumentException("{groupId}:{artifactId}:{version} can not be null at the same time");
            if (groupId != null && version != null) {
                throw new IllegalArgumentException("Null artifactId while groupId and version are not");
            }
        }
        Path res = null;
        if (groupId != null) {
            for (String s : groupId.split("\\.")) {
                res = toPath(res, s);
            }
        }
        if (artifactId != null) {
            res = toPath(res, artifactId);
        }
        if (version != null) {
            res = toPath(res, version);
        }
        return res;
    }

    private static String toStringPath(String groupId, String artifactId, String version) {
        Path res = toPath(groupId, artifactId, version);
        String s = FileSystems.getDefault().getSeparator();
        return s + res.toString() + s;
    }

    private static Path toPath(Path parent, String name) {
        Path res = Paths.get(name);
        if (parent == null) return res;
        return parent.resolve(res);
    }

    public static List<URI> list(Path target,
                                 BiPredicate<Path, String> tester,
                                 String... forbidden) throws IOException {
        return Files.find(target, 1, (p, b) -> b.isRegularFile())
                .filter(p -> p.getFileName().toString().endsWith(".jar"))
                .filter(p -> Arrays.stream(forbidden).noneMatch(s -> tester.test(p, s)))
                .map(Path::toUri)
                .collect(Collectors.toList());
    }

    public static List<URI> list(Path target, String... forbidden) throws IOException {
        return list(target, (p, s) -> p.getFileName().toString().contains(s), forbidden);
    }


    /**
     * Returns the names string of all jar entries that satisfy the given criterion.
     *
     * @param jar    {@link Path} path to jar, not {@code null}
     * @param filter {@link Predicate} criterion to filter result
     * @return List of names
     */
    public static List<String> getJarEntries(Path jar, Predicate<String> filter) {
        List<String> res = new ArrayList<>();
        try {
            Enumeration<JarEntry> entries = new JarFile(jar.toFile()).entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                String n = e.getName();
                if (filter.test(n)) {
                    res.add(n);
                }
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return res;
    }
}
