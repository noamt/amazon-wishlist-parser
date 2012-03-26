import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.ContentByteUtils
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor
import com.itextpdf.text.pdf.parser.RenderListener
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy

/**
 * @author Noam Y. Tenne
 */
@Grapes([
@Grab(group= 'com.itextpdf', module= 'itextpdf', version= '5.2.0'),
@GrabConfig(systemClassLoader=true)
])

class WishListPdfParser {

    public static void main(String[] args) {
        new WishListPdfParser().parse(args[0])
    }

    def parse(String pdfLocation) {
        def counter = 1;
        def reader = new PdfReader(pdfLocation)
        reader.numberOfPages.times { pageIndex ->
            RenderListener listener = new SimpleTextExtractionStrategy();
            PdfContentStreamProcessor processor = new PdfContentStreamProcessor(listener);
            def pageNumber = pageIndex + 1
            PdfDictionary pageDic = reader.getPageN(pageNumber)
            PdfDictionary resourcesDic = pageDic.getAsDict(PdfName.RESOURCES)
            processor.processContent(ContentByteUtils.getContentBytesForPage(reader, pageNumber), resourcesDic)
            listener.resultantText.split('\\d\\.').each { token ->
                if (token.contains('by')) {
                    def titleAndAuthorTokens = token.split('by')
                    def title = titleAndAuthorTokens[0].replaceAll('\n', ' ')
                    def author = titleAndAuthorTokens[1].replaceAll('\n', ' ')
                    if (author.contains('$')) {
                        author = author.substring(0, titleAndAuthorTokens[1].indexOf('$'))
                    }
                    println("${counter}. ${author}|${title}")
                    counter++
                } else if (token.contains('This title is no longer available')) {
                    //The title could have been removed from the catalog
                    println("${counter}. A non-available title")
                    counter++
                }
                //Don't print ignored tokens
                //else {
                    //println "Ignored ###${token}###"
                //}
            }
        }
    }
}
