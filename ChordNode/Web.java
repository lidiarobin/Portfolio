import java.io.*;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.xml.parsers.ParserConfigurationException;


class HTTPRequest {
	RequestType type;
	String resource;
	HTTPHeader headers[];
	
	String getHeaderValue(String key)
		{
		for (int i = 0; i < headers.length; i++)
			{
			if (headers[i].key.equals(key))
				return headers[i].value;
			}
		
		return null;
		}
	}

public class Web {
	
	static int RESPONSE_OK = 200;
	static int RESPONSE_NOT_FOUND = 404;
	static int RESPONSE_SERVER_ERROR = 501;
	
	FormMultipart formParser = new FormMultipart();

	INodeSupervisor ins;
	TurnIntoXML turnxml = new TurnIntoXML();

	//Web web = new Web();
	
	private void sendResponse(OutputStream output, int responseCode, String contentType, byte content[])
		{
		try {
			output.write(new String("HTTP/1.1 " + responseCode + "\r\n").getBytes());
			output.write("Server: Kitten Server\r\n".getBytes());
			if (content != null) output.write(new String("Content-length: " + content.length + "\r\n").getBytes());
			if (contentType != null) output.write(new String("Content-type: " + contentType + "\r\n").getBytes());
			output.write(new String("Connection: close\r\n").getBytes());
			output.write(new String("\r\n").getBytes());
			
			if (content != null) output.write(content);
			}
			catch (IOException e)
			{
			e.printStackTrace();
			}
		}

		

	//copy-pasted from week 2 step 4
	//need to add details to return valid html listing
	//all of the available files in the server directory
	//html should have a hyperlink to each file
	//each file should trigger a function getFile() which
	//will allow the client to download the file 

	public void originallistFiles(OutputStream output) throws MalformedURLException, IOException 
	{
		//System.out.println("Gets here");
		String html = "<html><body";
		String currentlink = "";

		//list directory path
		//directorypath.list
		//store in string array

		File dirPath = new File("/home/gruia/h-drive/4th year/SCC401/Coursework/files");
		//String stringPath = "./";

		String dirContent[] = dirPath.list();

		for (int i = 0; i < dirContent.length; i++)
		{
			System.out.println("File name: " + dirContent[i]);
			currentlink = "<a href='" + dirPath + "/" + dirContent[i] + "'download>" + dirContent[i] + "</a>";
			html = html + "<br>" + currentlink;

			//System.out.println(currentlink);
		}

		//byte[] RETURNTHIS = File.readAllBytes(fileObject.toPath());

		//System.out.println(currentlink);
		//getFile(currentlink, output);

		sendResponse(output, RESPONSE_OK, "text/html", html.getBytes());
		//sendResponse(output, RESPONSE_OK, "application/octet-stream", html.getBytes());

	}

	public void listFiles(OutputStream output) throws MalformedURLException, IOException 
	{
		//System.out.println("Gets here");
		String html = "<html><body>";
		//String currentlink = "";

		byte[] files = ins.getFiles();
		System.out.println(files);

		if(files != null)
			{	
				html = html + "<p> Files found </p>";
				String filename = "Filename.txt";
				String str = new String(files);
				html = html + str;
			}
		else
		{
			html = html + "<p> Did not find anything, please try again.";
		}

		html = html + "</body></html";

		sendResponse(output, RESPONSE_OK, "text/html", html.getBytes());

	}
	
	public void getFile(String filename, OutputStream output) throws MalformedURLException, IOException
	{
		//download file

		System.out.println("getfile accessed");
		File file = new File("/home/gruia/h-drive/4th year/SCC401/Coursework/files" + filename);
		System.out.println(filename);

		if(file.exists() && file.isFile())
		{
			byte[] fileBytes = new byte[(int) file.length()];
			FileInputStream input = new FileInputStream(file);
			input.read(fileBytes);
			input.close();

			sendResponse(output, RESPONSE_OK, "application/octet-stream", fileBytes);
		}
		
	}
	
	//example of a simple HTML page
	void page_index(OutputStream output)
		{
		sendResponse(output, RESPONSE_OK, "text/html", "<html>Hello!</html>".getBytes());
		}
	
	//example of a form to fill in, which triggers a POST request when the user clicks submit on the form
	void page_upload(OutputStream output)
		{
		String response = "";
		response = response + "<html>";
		response = response + "<body>";
		response = response + "<form action=\"/upload_do\" method=\"POST\" enctype=\"multipart/form-data\">";
		response = response + "<input type=\"file\" name=\"content\" required/>";
		response = response + "<input type=\"submit\" name=\"submit\"/>";
		response = response + "</form>";
		response = response + "</body>";
		response = response + "</html>";

		System.out.println(response);
		
		sendResponse(output, RESPONSE_OK, "text/html", response.getBytes());
		}

	//http://localhost:8080/tasks

	void page_tasks(OutputStream output)
	{
		String response = "";
		//String taskone = "<a href= '/taskone'> Click here to start task one</a>";
		//System.out.println(taskone);
		
		response = response + "<html>";
		response = response + "<head>";
		response = response + "<title> Task Selection </title>";
		response = response + "</head>";
		response = response + "<body>";
		response = response + "<h2> Task List: </h2>";
		response = response + "<h3> 1. Text Analyser</h3>";
		response = response + "<p> Check how many words a text has, what the most frequent word is, and the average word length. </p>";
		response = response + "<a href= '/taskone'> Click here to start task one</a>";
		response = response + "<h3> 2. [Placeholder] </h3>";
		response = response + "<p> [Placeholder] </p>";
		response = response + "<h3> 3. [Placeholder] </h3>";
		response = response + "<p> [Placeholder] </p>";
		response = response + "<h3> 4. [Placeholder] </h3>";
		response = response + "<p> [Placeholder] </p>";
		response = response + "</body>";
		response = response + "</html>";

		sendResponse(output, RESPONSE_OK, "text/html", response.getBytes());
	}

	void task_one(OutputStream output) throws RemoteException, IOException
	{
		String response = "";
		//String taskone = "<a href='" + "/home/gruia/h-drive/4th year/SCC401/Coursework/text.txt" + "' download> Start Task One</a>";

		byte[] taskOne = ins.getTaskOne();
		String stringBytes = new String(taskOne);
		System.out.println(stringBytes);

		/** 
		response = response + "<html>";
		response = response + "<head>";
		response = response + "<title> Task Selection </title>";
		response = response + "</head>";
		response = response + "<body>";
		response = response + "<h2> 1. Text Analyser</h3>";
		response = response + "<a href= 'taskone.xml' download> Download task one </a>";
		response = response + "</body>";
		response = response + "</html>";
		*/

		String xmlFile = "";

		xmlFile = xmlFile + "<?xml version=1.0?>";
		xmlFile = xmlFile + "<task>";
		xmlFile = xmlFile + "<p>";
		xmlFile = xmlFile + stringBytes;
		xmlFile = xmlFile + "</p>";
		xmlFile = xmlFile + "</task>";


		sendResponse(output, RESPONSE_OK, "application/octet-stream", xmlFile.getBytes());


	}
	
	//this function maps GET requests onto functions / code which return HTML pages
	void get(HTTPRequest request, OutputStream output) throws MalformedURLException, IOException
		{
			//getting the headers each time in get

			for (int i = 0; i < request.headers.length; i++)
			{
				//System.out.println("Header is " + request.headers[i].value);
			}
			
			if (request.resource.equals("/"))
			{
				page_index(output);
			}
			else if (request.resource.equals("/upload"))
			{
				page_upload(output);
			}
			else if(request.resource.equals("/files"))
			{
				listFiles(output);
			}
			else if(request.resource.equals("/tasks"))
			{
				page_tasks(output);
			}
			else if(request.resource.equals("/taskone"))
			{
				task_one(output);
			}
			else if(request.resource.equals("/download"))
			{
				getFile(null, output);
			}
			else
			{
				sendResponse(output, RESPONSE_NOT_FOUND, null, null);
			}	
		}
	
	//this function maps POST requests onto functions / code which return HTML pages
	void post(HTTPRequest request, byte payload[], OutputStream output) throws RemoteException, NotBoundException, IOException
		{
			Web web = new Web();
		if (request.resource.equals("/upload_do"))
			{
			//FormMultipart
			if (request.getHeaderValue("content-type") != null && request.getHeaderValue("content-type").startsWith("multipart/form-data"))
				{
				FormData data = formParser.getFormData(request.getHeaderValue("content-type"), payload);
				
				for (int i = 0; i < data.fields.length; i++)
					{
					System.out.println("field: " + data.fields[i].name);
					
					if (data.fields[i].name.equals("content"))
						{
							Registry registry = LocateRegistry.getRegistry();
							ins = (INodeSupervisor) registry.lookup("NodeSupervisor");
							System.out.println(ins);
							try 
							{
								//sends data back to user
								ins.receiveData(data.fields[i].content);
							} 
							catch (ParserConfigurationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							System.out.println(" -- filename: " + ((FileFormField) data.fields[i]).filename);

							//ins.parseData();
						}
					
					}
				
				sendResponse(output, RESPONSE_OK, "text/html", "<html>File sent, thanks!</html>".getBytes());
				}
				else
				{
				sendResponse(output, RESPONSE_SERVER_ERROR, null, null);
				}
			}
		}
	
}