package handler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class XMLHandler {
    public static String getXMLFromImage(String path) throws IOException {
        var mediaData = ImageIO.read(new File(path));

        // Преобразуем изображение в Base64
        var byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(mediaData, "png", byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        var base64ImageData = java.util.Base64.getEncoder().encodeToString(imageBytes);

        // Создаем XML-документ с элементом <ImageData>
        var dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            var doc = dBuilder.newDocument();

            // Создаем элемент <ImageData>
            var imageDataElement = doc.createElement("ImageData");
            imageDataElement.appendChild(doc.createTextNode(base64ImageData));
            doc.appendChild(imageDataElement);

            // Преобразуем XML-документ в строку
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(doc);
            ByteArrayOutputStream xmlOutputStream = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(xmlOutputStream);
            transformer.transform(domSource, result);

            return xmlOutputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void saveImageFromXMLData(String xmlData, String path) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // Преобразуем строку XML в объект Document
            ByteArrayInputStream input = new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8));
            Document doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();

            // Извлечение элемента <ImageData> из XML
            Element imageDataElement = (Element) doc.getElementsByTagName("ImageData").item(0);
            String base64ImageData = imageDataElement.getTextContent(); // Получаем строку с Base64 данными

            // Декодируем строку Base64 в байты
            byte[] imageBytes = Base64.getDecoder().decode(base64ImageData);

            // Сохранение байтов в файл по указанному пути
            try (FileOutputStream fos = new FileOutputStream(path)) {
                fos.write(imageBytes);
            }

            System.out.println("Image saved successfully at: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
