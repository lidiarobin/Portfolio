import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**the following class turns a file into an xml
 * it creates a document which it then fills with the values we've gotten
 * as we cannot have ints or floats, we will solely have strings
 * therefore i use the appropriate functions to turn our int/float into a string
 * once all is done it turns the file into a byte array
 * so that we can return it easily
 * but it is still structured the way an xmlfile would be
*/
public class TurnIntoXML 
{
    public byte[] getXMLFile(int countWords, String commonWord, float averageLength) throws IOException, ParserConfigurationException
    {
        try
        {
        
        String filename = "xmlfile";

        DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
        DocumentBuilder docbuild = builder.newDocumentBuilder();
        Document newDoc = docbuild.newDocument();
        Element finalDoc = newDoc.createElement(filename);
        newDoc.appendChild(finalDoc);

        TransformerFactory transFac = TransformerFactory.newInstance();
        Transformer transformer = transFac.newTransformer();

        Text auxCountWords = newDoc.createTextNode(Integer.toString(countWords));
        Text auxCommonWord = newDoc.createTextNode(commonWord);
        Text auxAverageLength = newDoc.createTextNode(Float.toString(averageLength));

        Element countWord = newDoc.createElement("Countwords");
        finalDoc.appendChild(countWord);
        finalDoc.appendChild(auxCountWords);

        Element comWord = newDoc.createElement("Commonword");
        finalDoc.appendChild(comWord);
        finalDoc.appendChild(auxCommonWord);
    
        Element averLen = newDoc.createElement("Averagelength");
        finalDoc.appendChild(averLen);
        finalDoc.appendChild(auxAverageLength);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DOMSource source = new DOMSource(newDoc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
        return output.toByteArray();
        }

        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        
    }
    
}
