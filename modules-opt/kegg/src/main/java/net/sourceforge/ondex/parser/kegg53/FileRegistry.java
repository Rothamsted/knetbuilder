package net.sourceforge.ondex.parser.kegg53;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author hindlem
 */
public class FileRegistry {

    private static Map<DataResource, FileIndex> resourcesIndex = new ConcurrentHashMap<DataResource, FileIndex>();

    private static FileRegistry fr;
    private String baseDir;

    /**
     * @param baseDir the reference dir to look for resources
     */
    private FileRegistry(String baseDir) {
        this.baseDir = baseDir;
        ExecutorService EXECUTOR = Executors.newFixedThreadPool(resources.length);
        List<Future<DataResource>> futures = new ArrayList<Future<DataResource>>();
        for (final DataResource resource : resources) {
            System.out.println("Processing resource: " + resource);
            futures.add(
                    EXECUTOR.submit(new Callable<DataResource>() {

                        private DataResource createIndex() {
                            try {
                                getIndex(resource);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return resource;
                        }

                        @Override
                        public DataResource call() throws Exception {
                            return createIndex();
                        }
                    })
            );
        }
        for (Future<DataResource> f : futures) {
            try {
                DataResource resource = f.get();
                System.out.println("Finished processing resource: " + resource);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            }
        }
        EXECUTOR.shutdown();
    }

    public static synchronized FileRegistry getInstance(String pathToKegg) {
        if (fr == null) {
            fr = new FileRegistry(pathToKegg);
        }
        return fr;
    }

    public enum DataResource {
        GENES(GENES_ZIP, GENES_FILE, GENES_DIR),
        KGML(KGML_ZIP, KGML_FILE, KGML_DIR),
        LIGAND(LIGAND_ZIP, LIGAND_FILE, LIGAND_DIR),
        PATHWAY(PATHWAY_ZIP, PATHWAY_FILE, PATHWAY_DIR),
        BRITE(BRITE_ZIP, BRITE_FILE, BRITE_DIR),
        MEDICUS(MEDICUS_ZIP, MEDICUS_FILE, MEDICUS_DIR);

        private String zipArchive;
        private String tarArchive;
        private String dir;

        /**
         * @param tarArchive a tar.gz compressed file archive
         * @param dir        a uncompressed dir archive
         * @param zipArchive a zip compressed file archive
         */
        private DataResource(String zipArchive, String tarArchive, String dir) {
            this.zipArchive = zipArchive;
            this.tarArchive = tarArchive;
            this.dir = dir;
        }

        /**
         * @return a tar.gz compressed file archive
         */
        public String getTarArchive() {
            return tarArchive;
        }

        /**
         * @return a compressed file archive
         */
        public String getZipArchive() {
            return zipArchive;
        }

        /**
         * @return a uncompressed dir archive
         */
        public String getDir() {
            return dir;
        }
    }

    public static String GENES_DIR = "genes";
    public static String KGML_DIR = "kgml";
    public static String LIGAND_DIR = "ligand";
    public static String PATHWAY_DIR = "pathway";
    public static String BRITE_DIR = "brite";
    public static String MEDICUS_DIR = "medicus";

    public static String GENES_FILE = GENES_DIR + ".tar.gz";
    public static String KGML_FILE = KGML_DIR + ".tar.gz";
    public static String LIGAND_FILE = LIGAND_DIR + ".tar.gz";
    public static String PATHWAY_FILE = PATHWAY_DIR + ".tar.gz";
    public static String BRITE_FILE = BRITE_DIR + ".tar.gz";
    public static String MEDICUS_FILE = MEDICUS_DIR + ".tar.gz";

    public static String GENES_ZIP = GENES_DIR + ".zip";
    public static String KGML_ZIP = KGML_DIR + ".zip";
    public static String LIGAND_ZIP = LIGAND_DIR + ".zip";
    public static String PATHWAY_ZIP = PATHWAY_DIR + ".zip";
    public static String BRITE_ZIP = BRITE_DIR + ".zip";
    public static String MEDICUS_ZIP = MEDICUS_DIR + ".zip";

    private static DataResource[] resources = new DataResource[]{
            DataResource.GENES,
            DataResource.KGML,
            DataResource.LIGAND,
            DataResource.PATHWAY,
            DataResource.BRITE,
            DataResource.MEDICUS
    };

    public FileIndex getIndex(DataResource resource) throws IOException {
        FileIndex index = resourcesIndex.get(resource);
        if (index == null) {
            if (new File(baseDir + File.separator + resource.getDir()).exists()) {
                index = new FileIndex(new File(baseDir + File.separator + resource.getDir()));
            } else if (new File(baseDir + File.separator + resource.getZipArchive()).exists()) {
                index = new FileIndex(new File(baseDir + File.separator + resource.getZipArchive()));
            } else if (new File(baseDir + File.separator + resource.getTarArchive()).exists()) {
                index = new FileIndex(new File(baseDir + File.separator + resource.getTarArchive()));
            } else {
                throw new IOException(resource + " does not exist at " + baseDir + " in the form of " +
                        resource.getDir() + ", " + resource.getZipArchive() + ", or " + resource.getTarArchive());
            }
            resourcesIndex.put(resource, index);
            return index;
        }
        return index;
    }

    public static void main(String[] args) {
        // try {
        String pathToKegg = "D:/Data/importdata/kegg53";

        FileRegistry fr = FileRegistry.getInstance(pathToKegg);
    }
}
