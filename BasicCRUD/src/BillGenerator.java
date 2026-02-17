import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
// If using Apache PDFBox, add these imports:
// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.apache.pdfbox.pdmodel.PDPage;
// import org.apache.pdfbox.pdmodel.PDPageContentStream;
// import org.apache.pdfbox.pdmodel.font.PDType1Font;
// import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class BillGenerator {

    private static String getCustomerNameByEmail(String encodedEmail) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("data/customers.csv"));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 5 && p[4].equals(encodedEmail)) { // p[4] is email
                    return p[1]; // p[1] is name
                }
            }
        } finally {
            br.close();
        }
        return "Unknown";
    }

    public static void generateBillPDF(String email, double total,
                                      String address,
                                      Map<String, Integer> cart)
            throws IOException {
        
        // For now, create a simple text-based PDF
        // You'll need to add PDFBox library for full PDF support
        
        // Simple approach: Create HTML and convert (or use PDFBox directly)
        // Here's a basic PDFBox example structure:
        
        /*
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("GROCERY STORE BILL");
        contentStream.endText();
        
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 700);
        contentStream.showText("Customer: " + email);
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("Address: " + address);
        contentStream.newLineAtOffset(0, -40);
        
        BufferedReader br = new BufferedReader(new FileReader("data/products.csv"));
        String line;
        int yPos = 600;
        
        while ((line = br.readLine()) != null) {
            for (String id : cart.keySet()) {
                if (line.startsWith(id + ",")) {
                    String[] p = line.split(",");
                    contentStream.showText(p[1] + " | Qty: " + cart.get(id));
                    contentStream.newLineAtOffset(0, -20);
                    yPos -= 20;
                }
            }
        }
        br.close();
        
        contentStream.showText("Total: ₹" + total);
        contentStream.endText();
        contentStream.close();
        
        document.save("bill.pdf");
        document.close();
        */
        
        // Temporary: Create a simple text file (replace with PDFBox code above)
        FileWriter fw = new FileWriter("bill.txt");

        String name = getCustomerNameByEmail(email);
        String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8);

        fw.write("===== GROCERY STORE BILL =====\n");
        fw.write("Customer Name: " + name + "\n");
        fw.write("Customer Email: " + decodedEmail + "\n\n");
        
        BufferedReader br = new BufferedReader(new FileReader("data/products.csv"));
        String line;
        
        while ((line = br.readLine()) != null) {
            for (String id : cart.keySet()) {
                if (line.startsWith(id + ",")) {
                    String[] p = line.split(",");
                    fw.write(p[1] + " | Qty: " + cart.get(id) + "\n");
                }
            }
        }
        br.close();
        
        fw.write("\nTotal: ₹" + total + "\n");
        fw.write("Delivery Address: " + address + "\n");
        fw.write("Status: Order Shipped\n");
        fw.close();
    }
}