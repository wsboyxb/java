package com.xb.html;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

public class HtmlParse {


	public static void test() throws MalformedURLException, IOException {
		String urlString = "http://bs.ustc.edu.cn/chinese/teacher.html";
		Source source = new Source(new URL(urlString));
		List<Element> bodyElements = source.getAllElements(HTMLElementName.TBODY);
		for (Element body : bodyElements) {
			List<Element> tableElements = body.getContent().getAllElements(HTMLElementName.TABLE);
			for (Element table : tableElements) {
				List<Element> tdElements = table.getContent().getAllElements(HTMLElementName.TD);
				for (Element td : tdElements) {
					Segment tdSegment = td.getContent();
					System.out.print(tdSegment.getTextExtractor());
				}
				System.out.println();
			}
		}
	}

	public static void test1(String urlString) throws MalformedURLException, IOException {
		Source source = new Source(new URL(urlString));
		source.fullSequentialParse();
		List<Element> elements = source.getAllElementsByClass("teacher_no");
		for (Element element : elements) {
			Element parentElement = element.getParentElement();
			List<Element> tdeElements = parentElement.getContent().getAllElements(HTMLElementName.TD);
			// for (Element td : tdeElements) {
			if (tdeElements.size() < 2) {
				continue;
			}
			Element tdName = tdeElements.get(0);
			Element tdLast = tdeElements.get(tdeElements.size() - 1);
			System.out.print(tdName.getContent().getTextExtractor() + "\t");
			System.out.print(tdLast.getContent().getTextExtractor());
			// }
			System.out.println();
		}
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		test1("http://bs.ustc.edu.cn/chinese/teacher.html");
	}
}
