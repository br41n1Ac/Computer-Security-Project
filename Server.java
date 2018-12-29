import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.util.HashMap;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

public class Server implements Runnable {
	private ServerSocket serverSocket;
	private static int numConnectedClients = 0;
	private static HashMap<String, Journal> journals = new HashMap<String, Journal>();
	private static HashMap<String, User> users = new HashMap<String, User>();

	public static void main(String args[]) throws IOException {
		System.out.println("\nServer Started\n");
		loadJournals();
		loadUsers();
		int port = 1337;
		// if (args.length >= 1) {
		// port = Integer.parseInt(args[0]);
		// }
		String type = "TLS";
		try {
			ServerSocketFactory ssf = getServerSocketFactory(type);
			ServerSocket ss = ssf.createServerSocket(port);
			((SSLServerSocket) ss).setNeedClientAuth(true); // enables Client authentication
			new Server(ss);
		} catch (IOException e) {
			System.out.println("Unable to start Server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();
	}

	public void run() {
		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			newListener();
			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
			String subject = cert.getSubjectDN().getName();
			String issuer = cert.getIssuerDN().getName();
			String serial = cert.getSerialNumber().toString();
			numConnectedClients++;
			User user = new User();
			System.out.println("Client connected");
			System.out.println("Client name (cert subject DN field): " + subject);
			System.out.println("issuer name: " + issuer);
			System.out.println("serial number: " + serial);
			System.out.println(numConnectedClients + " concurrent connection(s)\n");

			PrintWriter out;
			BufferedReader in;
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// StringBuilder sb = new StringBuilder();
			String clientMsg;
			String temp;
			while ((clientMsg = in.readLine()) != null) {
				user = commands(clientMsg, user, in, out);
				/*
				 * if (!user.isLoggedIn()) { if (clientMsg.equals("login")) { login(in, out,
				 * user); } else { out.println(""); out.flush(); } } else { if
				 * (clientMsg.equals("logout")) { logout(out, user); } else if
				 * (clientMsg.equals("print")) { out.println(user); out.flush(); } else if
				 * (clientMsg.equals("view")) { // displayJournal(in, out, user); StringBuilder
				 * sb = new StringBuilder(); for (String j : journals.keySet()) { if
				 * (user.hasReadPermission(journals.get(j))) { sb.append(j + " : "); } }
				 * System.out.println(sb.toString()); out.println(sb.toString());
				 * 
				 * } else { out.println(clientMsg); out.flush(); } }
				 */ }

			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			System.out.println("Client disconnected");
			System.out.println(numConnectedClients + " concurrent connection(s)\n");
		} catch (IOException e) {
			System.out.println("Client died: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	 	private User login(BufferedReader in, PrintWriter out) throws IOException {
		sayToClient("Enter username: ", out);
		String username = listenToClient(in);
		sayToClient("Enter password", out);
		String password = listenToClient(in);
		System.out.println("Password is");
		// String username = askClient("Enter username: ", in, out);
		// String password = askClient("Enter password", in, out);

		User user = users.get(username);
		System.out.println(user.checkCredentials(username, password));
		StringBuilder sb = new StringBuilder();
		sb.append(" ; ");
		if (user != null && user.checkCredentials(username, password)) {
			for (Journal j : journals.values()) {
				if (user.hasReadPermission(j)) {
					sb.append(": " + j.getJournalId());
				}

			}
			System.out.println("efter append journals");
			out.println("You are now logged in" + sb.toString());
			out.flush();
			log("User logged in", user);

			return user;
		} else {
			out.println("Login failed; unknown username or incorrect password");
			out.flush();
			return new User();
		}
	}

	private void sayToClient(String outMessage, PrintWriter out) throws IOException {
		out.println(outMessage);
		out.flush();
	}

	/**
	 * Lyssnar på klienten
	 *
	 * @param in
	 * @return returnerar det som skrevs i klienten
	 * @throws IOException
	 */
	private String listenToClient(BufferedReader in) throws IOException {
		String inMessage;
		while ((inMessage = in.readLine()) == null) {
		}
		return inMessage;
	}

	private User commands(String command, User user, BufferedReader in, PrintWriter out) throws IOException {
		if (!user.isLoggedIn()) {
			if (command.equals("login")) {
				return login(in, out);
			} else {
				sayToClient("", out);
			}
		} else {
			switch (command) {
				case "logout":
					user = logout(user, out);
					break;
				case "read":
					readJournal(user, in, out);
					break;
				case "write":
					writeJournal(user, in, out);
					break;
				case "create":
					createJournal(user, in, out);
					break;
				case "delete":
					deleteJournal(user, in, out);
					break;
				default:
					sayToClient(command, out);
					break;
			}
		}
		return user;
	}

	private void readJournal(User user, BufferedReader in, PrintWriter out) throws IOException {
		sayToClient("Enter journal ID: ", out);
		String journalId = listenToClient(in);
		while (!journals.containsKey(journalId)) {
			sayToClient("Journal ID not found, please enter a different journal ID:", out);
			journalId = listenToClient(in);
		}

		Journal journal = journals.get(journalId);
		if (user.hasReadPermission(journal)) {
			
			out.println("journal info ;" + journal.getInfo());
			log("User read file " + journal.getJournalId(), user);
		} else {
			out.println("Permission denied");
		}
		out.flush();
	}

	private void writeJournal(User user, BufferedReader in, PrintWriter out) throws IOException {
		if (user.hasWritePermission()) {
			sayToClient("Enter journal ID: ", out);
			String journalId = listenToClient(in);
			while (!journals.containsKey(journalId)) {
				sayToClient("Journal ID not found, please enter a different journal ID:", out);
				journalId = listenToClient(in);
			}

			Journal journal = journals.get(journalId);
			sayToClient("Enter journal info: ", out);
			String newInfo = listenToClient(in);
			if (journal.writeInfo(newInfo)) {
				out.println("Journal updated");
				log("User wrote to file " + journal.getJournalId(), user);
			} else {
				out.println("An error occurred during the writing process, please try again");
			}
		} else {
			out.println("Permission denied");
		}
		out.flush();
	}

	private void createJournal(User user, BufferedReader in, PrintWriter out) throws IOException {
		if (user.hasCreatePermission()) {
			sayToClient("Enter journal ID: ", out);
			String journalId = listenToClient(in);
			while (journals.containsKey(journalId)) {
				sayToClient("Journal ID taken, please enter a different journal ID:", out);
				listenToClient(in);
			}

			Journal journal = new Journal(journalId);
			journal.setDoctorId(user.getUserId());
			sayToClient("Enter patient ID:", out);
			journal.setPatientId(listenToClient(in));
			sayToClient("Enter nurse ID:", out);
			journal.setNurseId(listenToClient(in));
			sayToClient("Enter hospital division:", out);
			journal.setHospitalDivision(listenToClient(in));
			sayToClient("Enter journal info:", out);
			journal.setInfo(listenToClient(in));

			if (journal.createFile()) {
				journals.put(journal.getJournalId(), journal);
				out.println("Journal created");
				log("User created file " + journal.getJournalId(), user);
			} else {
				out.println("An error occurred during the creation process, please try again");
			}
		} else {
			out.println("Permission denied");
		}
		out.flush();
	}

	private void deleteJournal(User user, BufferedReader in, PrintWriter out) throws IOException {
		if (user.hasDeletePermission()) {
			sayToClient("Enter journal ID: ", out);
			String journalId = listenToClient(in);
			while (!journals.containsKey(journalId)) {
				sayToClient("Journal ID not found, please enter a different journal ID:", out);
				journalId = listenToClient(in);
			}

			Journal journal = journals.get(journalId);
			if (journal.deleteFile()) {
				journals.remove(journal.getJournalId());
				out.println("Journal deleted");
				log("User deleted file " + journal.getJournalId(), user);
			} else {
				out.println("An error occurred during the deletion process, please try again");
			}
		} else {
			out.println("Permission denied");
		}
		out.flush();
	}


	private void log(String text, User user) throws IOException {
		Path path = Paths.get("/Users/simonakesson/git/eita25/server/log/auditlog.txt");
		String entry = user.getUserId() + ": " + text + "\n";
		Files.write(path, entry.getBytes(), StandardOpenOption.APPEND);
	}

	private String askClient(String message, BufferedReader in, PrintWriter out) throws IOException {
		String clientMsg;
		out.println(message);
		out.flush();
		while ((clientMsg = in.readLine()) == null) {
		}
		return clientMsg;
	}
	
	

	private void newListener() {
		(new Thread(this)).start();
	} // calls run()

	private static void loadJournals() throws IOException {
		File folder = new File("/Users/simonakesson/git/eita25/server/journals");
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			Path path = Paths.get(file.toString());
			String journalId = file.getName();
			journalId = journalId.substring(0, journalId.length() - 4);
			Journal journal = new Journal(journalId);
			journal.populate(path);
			journals.put(journal.getJournalId(), journal);
		}
	}

	private static void loadUsers() throws IOException {
		Path path = Paths.get("/Users/simonakesson/git/eita25/server/users/users.txt");
		Files.lines(path).forEach(s -> {
			String[] fields = s.split(";");
			User user = new User();
			user.populate(fields);
			users.put(user.getUsername(), user);
		});
	}

	private User logout(User user, PrintWriter out) throws IOException {
		out.println("You are now logged out");
		out.flush();
		log("User logged out", user);
		return new User();
	}

	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals("TLS")) {
			SSLServerSocketFactory ssf = null;
			try { // set up key manager to perform Server authentication
				SSLContext ctx = SSLContext.getInstance("TLS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				char[] password = "password".toCharArray();
				try {
					ks.load(new FileInputStream("/Users/simonakesson/eita25/server/certificates/serverkeystore"),
							password); // keystore password (storepass)
					ts.load(new FileInputStream("/Users/simonakesson/eita25/server/certificates/servertruststore"),
							password); // truststore password (storepass)
				} catch (Exception e) {
					ks.load(new FileInputStream("/Users/simonakesson/eita25/server/certificates/serverkeystore"),
							password); // keystore password (storepass)
					ts.load(new FileInputStream("/Users/simonakesson/eita25/server/certificates/servertruststore"),
							password); // truststore password (storepass)
				}
				kmf.init(ks, password); // certificate password (keypass)
				tmf.init(ts); // possible to use keystore as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;
	}
}
