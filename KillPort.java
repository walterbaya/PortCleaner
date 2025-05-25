import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class KillPort {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el número de puerto: ");
        int port = scanner.nextInt();

        try {
            String pid = getProcessId(port);
            if (pid != null) {
                System.out.println("PID encontrado: " + pid);
                killProcess(pid);
                System.out.println("Proceso terminado exitosamente.");
            } else {
                System.out.println("No se encontró ningún proceso usando el puerto " + port);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getProcessId(int port) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return getWindowsProcessId(port);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            return getUnixProcessId(port);
        } else {
            throw new UnsupportedOperationException("Sistema operativo no soportado");
        }
    }

    private static String getWindowsProcessId(int port) throws IOException {
        Process p = new ProcessBuilder("cmd.exe", "/c", "netstat -ano").start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = r.readLine()) != null) {
            line = line.trim();
            if ((line.contains("LISTENING") || line.contains("ESTABLISHED")) &&
                line.matches(".*:" + port + "\\b.*")) {
                String[] parts = line.split("\\s+");
                return parts[parts.length - 1];
            }
        }
        return null;
    }

    private static String getUnixProcessId(int port) throws IOException {
        Process p = new ProcessBuilder("sh", "-c", "lsof -t -i:" + port).start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String pid = r.readLine();
        return (pid != null && !pid.isEmpty()) ? pid : null;
    }

    private static void killProcess(String pid) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        Process p;

        if (os.contains("win")) {
            p = new ProcessBuilder("cmd.exe", "/c", "taskkill /F /PID " + pid).start();
        } else {
            p = new ProcessBuilder("sh", "-c", "kill -9 " + pid).start();
        }

        // Leer salida de error para obtener detalles
        BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        StringBuilder errorMsg = new StringBuilder();
        String l;
        while ((l = err.readLine()) != null) {
            errorMsg.append(l).append(System.lineSeparator());
        }

        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(
                "Fallo al terminar el proceso. Código de error: " + exitCode +
                (errorMsg.length() > 0 ? "\nDetalles: " + errorMsg.toString().trim() : "") +
                "\n\nAsegurate de ejecutar este programa en una consola con permisos de administrador."
            );
        }
    }
}
