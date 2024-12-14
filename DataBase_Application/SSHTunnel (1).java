import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.AclFileAttributeView;

public class SSHTunnel implements AutoCloseable {

    public static record Settings(
        String host, int port, String username,
        int localPort,
        String dbHost, int dbPort,
        String keyFile, String hostKeyFile
    ) {
        public Settings() {
            this(
                "cs.westminsteru.edu", 2322, "student",
                -1,
                "localhost", 3306,
                "id_ecdsa.cmpt307", "host_key.pub"
            );
        }
    }

    private Settings settings;
    private int actualLocalPort = -1;
    private Process process;

    public SSHTunnel(Settings settings) {
        this.settings = settings;
        open();
    }

    public SSHTunnel() {
        this(new Settings());
    }

    public void setSettings(Settings settings) {
        if (isOpen())
            throw new IllegalStateException("Cannot change settings while tunnel is open");

        this.settings = settings;
    }

    public Settings getSettings() {
        return settings;
    }

    public int getLocalPort() {
        return actualLocalPort;
    }

    public void open() {
        if (isOpen())
            throw new IllegalStateException("Tunnel already open");
        
        final var s = this.settings;

        // Check that the key files exist
        if (!Files.isRegularFile(Path.of(s.keyFile)))
            throw new RuntimeException(String.format("SSH private key file not found: %s", s.keyFile));
        else
            // Also make sure the key file has the right permissions (otherwise ssh will bail)
            enforceKeyFilePermissions(s.keyFile);

        if (!Files.isRegularFile(Path.of(s.hostKeyFile)))
            throw new RuntimeException(String.format("SSH host public key file not found: %s", s.hostKeyFile));

        if (s.localPort <= 0)
            actualLocalPort = new Random().nextInt(30_000, 65_000);
        else
            actualLocalPort = s.localPort;

        Path keyfilePath = null;
        try {
            // TODO: way to check for ssh in PATH?
            this.process = new ProcessBuilder(List.of("ssh",
                // identity (private key)
                "-i", s.keyFile,
                // server identity (public key)
                "-o", String.format("UserKnownHostsFile=%s", s.hostKeyFile),
                // local forward for database port
                "-L", String.format("%d:%s:%d", actualLocalPort, s.dbHost, s.dbPort),
                // port to connect to
                "-p", "" + s.port,
                // no terminal needed
                "-T",
                // username/host to connect to
                String.format("%s@%s", s.username, s.host)
            )).redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();

            Thread.sleep(500);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to invoke ssh", ex);
        } catch (InterruptedException ex) {
            // won't happen
        }
    }

    @Override
    public void close() {
        if (isOpen()) {
            process.destroy();
            process = null;
        }
    }

    public boolean isOpen() {
        return process != null;
    }

    /** Make sure the key file permissions aren't too open, otherwise ssh will bail! */
    private static void enforceKeyFilePermissions(String keyFile) {
        var keyPath = Path.of(keyFile);

        try {
            // No opportunity to test this code (and I don't think ssh enforces privileges
            // in Windows anyway!)
            /*
            var aclAttrView = Files.getFileAttributeView(keyPath, AclFileAttributeView.class);
            if (aclAttrView != null) {
                System.out.println("Using ACL attributes");
                var owner = aclAttrView.getOwner();
                System.out.printf("Owner: %s", owner);
                System.out.println("Existing ACLs:");
                aclAttrView.getAcl().stream().forEach(System.out::println);
                // Retain only those ACL entries pertaining to the owner
                aclAttrView.setAcl(
                    aclAttrView.getAcl().stream()
                        .filter(acl -> owner.equals(acl.principal()))
                        .toList()
                );
            }
            */

            var posixAttrView = Files.getFileAttributeView(keyPath, PosixFileAttributeView.class);
            if (posixAttrView != null) {
                posixAttrView.setPermissions(EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE
                ));
            }

        } catch (IOException ex) {
            throw new RuntimeException("Unable to set ssh key file permissions", ex);
        }
    }
}
