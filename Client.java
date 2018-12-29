import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.Observable;
import java.util.Observer;

/*
 * This example shows how to set up a key manager to perform Client
 * authentication.
 *
 * This program assumes that the Client is not inside a firewall.
 * The application can be modified to connect to a Server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class Client extends Observable{

	private static String username, journalID, password;
	private static boolean login = false;
	private static boolean willRead = false;
	private static boolean acknowledgement = false;
	private static String selectedJournal, journalInfo;
	

	public static void main(String[] args) throws Exception {
		String host = null;
		int port = -1;
		// for (int i = 0; i < args.length; i++) {
		// System.out.println("args[" + i + "] = " + args[i]);
		// }
		// if (args.length < 2) {
		// System.out.println("USAGE: java Client host port");
		// System.exit(-1);
		// }
		try { /* get input parameters */
			host = "localhost";
			port = 1337;
		} catch (IllegalArgumentException e) {
			System.out.println("USAGE: java Client host port");
			System.exit(-1);
		}

		try { /* set up a key manager for Client authentication */
			SSLSocketFactory factory = null;
			try {
				char[] password = "password".toCharArray();
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				SSLContext ctx = SSLContext.getInstance("TLS");
				try {
					ks.load(new FileInputStream("/Users/simonakesson/eita25/client/clientkeystore"), password); // keystore																											// password
																												// (storepass)
					ts.load(new FileInputStream("/Users/simonakesson/eita25/client/clienttruststore"), password); // truststore																											// password
																													// (storepass);
				} catch (Exception e) {
					ks.load(new FileInputStream("/Users/simonakesson/eita25/client/clientkeystore"), password); // keystore																												// password																												// (storepass)
					ts.load(new FileInputStream("/Users/simonakesson/eita25/client/clienttruststore"), password); // truststore
																													// password
																													// (storepass);
				}
				kmf.init(ks, password); // User password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				factory = ctx.getSocketFactory();
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
			System.out.println("\nsocket before handshake:\n" + socket + "\n");

			/*
			 * send http request
			 *
			 * See SSLSocketClient.java for more information about why there is a forced
			 * handshake here when using PrintWriters.
			 */
			socket.startHandshake();

			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
			String subject = cert.getSubjectDN().getName();
			String issuer = cert.getIssuerDN().getName();
			String serial = cert.getSerialNumber().toString();
			System.out.println("certificate name (subject DN field) on certificate received from Server:\n" + subject);
			System.out.println("issuer name: " + issuer);
			System.out.println("serial number: " + serial);
			System.out.println("socket after handshake:\n" + socket + "\n");
			System.out.println("secure connection established\n\n");

			BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String msg;

			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			System.out.println("Kommandon man kan köra:\n" + "quit: Avslutar Client\n"
					+ "login: För att logga in om man är utloggad\n" + "logout: För att logga ut om man är inloggad\n"
					+ "print: Skriver ut user info om man är inloggad\n"
					+ "view: För att skriva ut önskad journal (kräver rätt rättigheter\n");
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");

			int counter = 0;
			while (true) {
				if (login) {
					out.println("login");
					System.out.println("login+");
					togglelogin(false);
				}
				counter++;
				msg = in.readLine();	
				System.out.println(msg);
				if (msg.equals("Enter username: ") && counter == 1) {
					out.println(username);
					System.out.println("Sent user");
				}				
				if (msg.equals("Enter password") && counter == 2) {
					out.println(password);
					System.out.println("Sent pass");
				}
				if(msg.equals("Enter journal ID: ")) {
					out.println(selectedJournal);
					System.out.println("journal");
				}
				if(msg.contains("journal info ")) {
					journalInfo = msg.split(" ;")[1];
					
				}
				
				if (willRead) {
					out.println("read");
					read(false);
				}
				if (msg.contains("You are now logged in")) {
					System.out.println("here?");
					acknowledgement = true;
					journalID = msg.split(" ; ")[1];
					System.out.println(journalID);
					
					out.println("read");
					break;
					
				}
				System.out.println(counter);

				
				if (msg.equals("Login failed; unknown username or incorrect password")) {
					acknowledgement = false;
					break;
				}
				
			}
		
			
			in.close();
			out.close();
			read.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setPassword(String s) {
		password = s;
	}

	public void setUsername(String s) {
		username = s;
	}

	public static boolean togglelogin(boolean a) {
		return login = a;
	}
	public static boolean read(boolean a) {
		return willRead = a;
		
	}

	public static boolean getAck() {
		return acknowledgement;
	}
	public static String getJournalInfo() {
		return journalInfo;
	}
	
	public String[] getJournals() {	
		String[] s = journalID.split(": ");
		String[] a = new String [s.length-1];
		for(int i = 1; i < s.length;i++) {
	
			a[i-1]=s[i];
		}

		return a;
	}
	public String setSelectedJournal(String journal) {
		return selectedJournal=journal;
	}

}
