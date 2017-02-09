importPackage(javax.xml.parsers)

/* usage:
	dbf = createDocumentBuilderFactory()
	var db = dbf.newDocumentBuilder()
	var dom = db.parse("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pmc&term="+term+"&retmode=xml")
	var docEle = dom.getDocumentElement()
	var nl = docEle.getElementsByTagName("Id")

	// one-time-read:
	// var doc = createDocumentBuilderFactory().newDocumentBuilder().parse(pathxml).getDocumentElement()
	
	var refTitle = getXMLElementContent(docEle, "article-title")
	var refAuthor = getXMLElementContent(docEle, "contrib", "contrib-type", "author")

*/
function createDocumentBuilderFactory() {
	// turn all xml validation off (performance!)
	var dbf = DocumentBuilderFactory.newInstance()
	dbf.setNamespaceAware(false);
	dbf.setValidating(false);
	dbf.setFeature("http://xml.org/sax/features/namespaces", false);
	dbf.setFeature("http://xml.org/sax/features/validation", false);
	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	return dbf;
}

function getXMLElementContent(docEle, tagName) {
	return getXMLElementContent(docEle, tagName, null, null)
}
function getXMLElementContent(docEle, tagName, attrName, attrValue) {
	var el=getXMLElement(docEle, tagName, attrName, attrValue)
	if(el==null)
		return null
	return el.getTextContent()
}

function getXMLElement(docEle, tagName, attrName, attrValue) {
	var item=null
	var nodeList = docEle.getElementsByTagName(tagName)
	for(var i=0; i<nodeList.getLength();i++) {
		var item = nodeList.item(i)
		if(attrName!=null && attrValue!=null) {
			if(!item.hasAttribute(attrName))
				continue
			if(!attrValue.equals(item.getAttribute(attrName)))
				continue
		}
		return item
	}
	return null
}
