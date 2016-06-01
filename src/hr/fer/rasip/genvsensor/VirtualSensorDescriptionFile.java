package hr.fer.rasip.genvsensor;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class generates Virtual sensor description file for GSN(Global Sensor
 * Networks) system. It was created following the DTD (Document Type Definition)
 * that is available on this link https://github.com/LSIR/gsn/blob/documentations/book-of-gsn/chapters/ch-quickref/figures/vs-quick-ref.pdf?raw=true
 * 
 * @author Luka Dulčić
 *
 */
public class VirtualSensorDescriptionFile {

	private Document virtualSensor;

	private Element rootElement;//<virtual-sensor>

	private Element processingClass;
	private Element initParams;
	private Element uniqueTimestamps;
	private Element outputStructure;
	private Element outputSpecifications;
	private Element webInput;

	private Element description;

	private Element lifeCycle;

	private Element addressing;

	private Element storage;

	private Element streams;
	
	private boolean fileUploaded;

	
	/**
	 * This constructor is used when needed to create new VSD file.
	 * 
	 * @throws ParserConfigurationException
	 */
	public VirtualSensorDescriptionFile() throws ParserConfigurationException {
		virtualSensor = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		virtualSensor.appendChild(virtualSensor.createComment("This file was generated on " + new Date()));
		rootElement = virtualSensor.createElement("virtual-sensor");
		virtualSensor.appendChild(rootElement);
		addressing = virtualSensor.createElement("addressing");
		streams = virtualSensor.createElement("streams");
	}
	
	/**
	 * This constructor is used when needed to load existing VSD
	 * and use its data or modify it.
	 * 
	 * @param vsd
	 * 	file which should be XML virtual sensor description file
	 * @throws ParserConfigurationException
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public VirtualSensorDescriptionFile(File vsd) throws ParserConfigurationException, SAXException, IOException, VirtualSensorException {
		virtualSensor = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(vsd);
		virtualSensor.normalize();
		parseVSDfile(vsd);
		fileUploaded = true;
	}
	
	/**
	 * Loads VSD from file given in <code>vsd</code>.
	 * 
	 * @param vsd virtual sensor description file
	 * @throws VirtualSensorException if <code>vsd</code> is not valid VSD
	 */
	private void parseVSDfile(File vsd) throws VirtualSensorException {
		//<virtual-sensor>
		rootElement = virtualSensor.getDocumentElement();
		if(!rootElement.getNodeName().equals("virtual-sensor")) {
			throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
					+ "Virtual sensor root tag must be \"virtual-sensor\".");
		}
		
		NodeList tmp = null;
		
		//<processing-class>
		tmp = rootElement.getElementsByTagName("processing-class");
		if(tmp.getLength() > 1) {
			throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
					+ "Virtual sensor must only have one processing class.");
		} else if(tmp.getLength() == 1) {
			processingClass = (Element)tmp.item(0);
			
			//<class-name>
			tmp = processingClass.getElementsByTagName("class-name");
			if(tmp.getLength() == 0 || tmp.item(0).getTextContent().trim().isEmpty()) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "Processing class must have <class-name> tag with valid content.");
			} else if(tmp.getLength() > 1) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "Processing class must have only one <class-name> tag.");
			}
			
			//<init-params>
			tmp = processingClass.getElementsByTagName("init-params");
			if(tmp.getLength() > 1) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "Processing class must have only one <init-params> tag.");
			} else if(tmp.getLength() == 1) {
				initParams = (Element)tmp.item(0);
			}
			
			//<unique-timestamps>
			tmp = processingClass.getElementsByTagName("unique-timestamps");
			if(tmp.getLength() > 1) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "Processing class must have only one <unique-timestamps> tag.");
			} else if(tmp.getLength() == 1) {
				uniqueTimestamps = (Element)tmp.item(0);
			}
			
			//<output-structure>
			tmp = processingClass.getElementsByTagName("output-structure");
			if(tmp.getLength() == 0) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "Processing class must have <output-structure> tag.");
			} else if(tmp.getLength() > 1) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "Processing class must only have one <output-structure> tag");
			} else if(tmp.getLength() == 1) {
				outputStructure = (Element)tmp.item(0);
			}
			
			//<output-specification>
			tmp = processingClass.getElementsByTagName("output-specification");
			if(tmp.getLength() > 1) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "Processing class must have only one <output-speciffication> tag.");
			} else if(tmp.getLength() == 1) {
				outputSpecifications = (Element)tmp.item(0);
			}
			
			//<web-input>
			tmp = processingClass.getElementsByTagName("web-input");
			if(tmp.getLength() > 1) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "Processing class must have only one <web-input> tag.");
			} else if(tmp.getLength() == 1) {
				webInput = (Element)tmp.item(0);
			}
		}
		
		//<description>
		tmp = rootElement.getElementsByTagName("description");
		if(tmp.getLength() > 1) {
			throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
					+ "Virtual sensor must only have one description.");
		} else if(tmp.getLength() == 1) {
			description = (Element)tmp.item(0);
		}
		
		//<life-cycle>
		tmp = rootElement.getElementsByTagName("life-cycle");
		if(tmp.getLength() > 1) {
			throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
					+ "Virtual sensor must only have one <life-cycle> tag.");
		} else if(tmp.getLength() == 1) {
			lifeCycle = (Element)tmp.item(0);
			if(lifeCycle.getAttribute("pool-size").isEmpty()) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "<life-cycle> tag must have valid pool-size attribute.");
			}
		}
		
		//<addressing>
		tmp = rootElement.getElementsByTagName("addressing");
		if(tmp.getLength() > 1) {
			throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
					+ "Virtual sensor must only have one <addressing> tag.");
		}
		
		//<storage>
		tmp = rootElement.getElementsByTagName("storage");
		if(tmp.getLength() > 1) {
			throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
					+ "Virtual sensor must only have one <storage> tag.");
		} else if(tmp.getLength() == 1) {
			storage = (Element)tmp.item(0);
			if(storage.getAttribute("history-size").isEmpty()) {
				throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
						+ "<storage> tag must have one history-size attribute with valid value.");
			}
		}
		
		//<streams>
		tmp = rootElement.getElementsByTagName("streams");
		if(tmp.getLength() == 0) {
			throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
					+ "Virtual sensor must have one <streams> tag.");
		} else if(tmp.getLength() > 1) {
			throw new VirtualSensorException(vsd.getName() + " is not valid GSN virtual sensor file!\n"
					+ "Virtual sensor must only have one <streams> tag.");
		} else if(tmp.getLength() == 1) {
			streams = (Element)tmp.item(0);
		}
	}

	
	// ROOT TAG ATTRIBUTES

	/**
	 * Sets virtual sensor name.
	 * 
	 * @param name virtual sensor name
	 */
	public void setVSName(String name) {
		rootElement.setAttribute("name", name);
	}
	
	/**
	 * Returns the name of virtual sensor.
	 * 
	 * @return name of virtual sensor.
	 */
	public String getVSName() {
		return rootElement.getAttribute("name");
	}

	/**
	 * Sets virtual sensor <code>priority</code> attribute.
	 * 
	 * @param priority value of <code>priority</code> attribute
	 */
	public void setVSPriority(int priority) {
		int acceptablePriority;
		if (priority < 0) {
			acceptablePriority = 0;
		} else if (priority > 20) {
			acceptablePriority = 20;
		} else {
			acceptablePriority = priority;
		}
		rootElement.setAttribute("priority",
				String.valueOf(acceptablePriority));
	}
	
	/**
	 * Returns value of virtual sensor <code>priority</code> attribute.
	 * 
	 * @return value of priority attribute
	 */
	public Integer getVSPriority() {
		String tmp = rootElement.getAttribute("priority");
		if(!tmp.isEmpty()) {
			return Integer.parseInt(tmp);
		} else {
			return null;
		}
	}

	/**
	 * Sets virtual sensor <code>protected</code> attribute.
	 * 
	 * @param value value of protected attribute
	 */
	public void setVSProtectedProperty(String value) {
		rootElement.setAttribute("protected", value);
	}
	
	/**
	 * Returns value of virtual sensor <code>protected</code> attribute.
	 * 
	 * @return value of protected attribute
	 */
	public String getVSProtectedProperty() {
		return rootElement.getAttribute("protected");
	}

	/**
	 * Sets virtual sensor <code>publish-to-microsoft-research-map</code> attribute.
	 * 
	 * @param value value of <code>publish-to-microsoft-research-map</code> attribute
	 */
	public void setPublishToMicrosoftResearchMap(String value) {
		rootElement.setAttribute("publish-to-microsoft-research-map",
				value);
	}
	
	/**
	 * Returns value of virtual sensor <code>publish-to-microsoft-research-map</code> attribute.
	 * 
	 * @return value of virtual sensor <code>publish-to-microsoft-research-map</code> attribute
	 */
	public String getPublishToMicrosoftResarchMap() {
		return rootElement.getAttribute("publish-to-microsoft-research-map");
	}
	
	/**
	 * Sets virtual sensor <code>timezone</code> attribute.
	 * 
	 * @param timezone value of <code>timezone</code> attribute
	 */
	public void setTimezone(String timezone) {
		rootElement.setAttribute("timezone", timezone);
	}
	
	/**
	 * Returns value of virtual sensor <code>timezone</code> attribute.
	 * 
	 * @return value of virtual sensor <code>timezone</code> attribute
	 */
	public String getTimezone() {
		return rootElement.getAttribute("timezone");
	}

	// PROCESSING-CLASS

	/**
	 * Sets value of <code>&ltclass-name&gt</code> element in VSD
	 * as a child of <code>&ltprocessing-class&gt</code> element.
	 * 
	 * @param className value of <code>&ltclass-name&gt</code> element
	 */
	public void setProcessingClass(String className) {
		if (processingClass == null)
			processingClass = virtualSensor
					.createElement("processing-class");

		Element element = virtualSensor.createElement("class-name");
		element.setTextContent(className);
		processingClass.appendChild(element);
	}
	
	/**
	 * Removes <code>&ltprocessing-class&gt</code> element from VSD.
	 */
	public void removeProccesingClass() {
		if(processingClass != null) {
			processingClass = null;
		}
	}

	// INIT-PARAMS

	/**
	 * Adds <code>&ltparam name="<i>name</i>"&gt<i>value</i>&ltparam&gt</code> element
	 * as a child to <code>&ltinit-params&gt</code> element.
	 * 
	 * @param param name of parameter
	 * @param value value of parameter
	 * @throws VirtualSensorException if <code>&ltprocessing-class&gt</code> element doesn't exist
	 */
	public void addInitParam(String param, String value) throws VirtualSensorException {
		if (processingClass == null)
			throw new VirtualSensorException(
					"You must set processing class before init params");
		if (initParams == null) {
			initParams = virtualSensor.createElement("init-params");
			processingClass.appendChild(initParams);
		}

		Element element = virtualSensor.createElement("param");
		element.setAttribute("name", param);
		element.setTextContent(value);
		initParams.appendChild(element);
	}
	
	/**
	 * Returns value of parameter.
	 * 
	 * @param paramName name of parameter
	 * @return value if parameter exists <code>null</code> otherwise
	 */
	public String getInitParam(String paramName) {
		if(processingClass == null || initParams == null) {
			return null;
		}
		
		NodeList tmp = initParams.getChildNodes();
		Element param = null;
		for (int i = 0; i < tmp.getLength(); i++) {
			if(tmp.item(i).getNodeType() == Node.ELEMENT_NODE) {
				param = (Element)tmp.item(i);
				if(param.getAttribute("name").equals(paramName)) {
					return param.getTextContent();
				}
			}
		}
		
		return null;
	}

	// UNIQUE-TIMESTAMPS

	/**
	 * Sets value of <code>&ltunique-timestamps&gt</code> element.
	 * 
	 * @param value value of <code>&ltunique-timestamps&gt</code> element 
	 * @throws VirtualSensorException if <code>&ltprocessing-class&gt</code> element doesn't exist
	 */
	public void setUniqueTimestamps(boolean value) throws VirtualSensorException {
		if (processingClass == null)
			throw new VirtualSensorException(
					"You must set processing class before unique-timestamps");
		if (uniqueTimestamps == null) {
			uniqueTimestamps = virtualSensor
					.createElement("unique-timestamps");
			processingClass.appendChild(uniqueTimestamps);
		}
		uniqueTimestamps.setTextContent(String.valueOf(value));
	}
	
	/**
	 * Returns value of <code>&ltunique-timestamps&gt</code> element.
	 * 
	 * @return value of <code>&ltunique-timestamps&gt</code> element as a String 
	 * 	   representing boolean value. If this element doesn't exist <code>null</code>
	 * 	   will be returned
	 * @throws VirtualSensorException if <code>&ltprocessing-class&gt</code> element doesn't exist
	 */
	public String getUniqueTimestamps() throws VirtualSensorException {
		if (processingClass == null)
			throw new VirtualSensorException(
					"You must set processing class before unique-timestamps");
		if(uniqueTimestamps != null) {
			return uniqueTimestamps.getTextContent();
		}
		return null;
	}

	// OUTPUT STRUCTURE

	/**
	 * Adds <code>&ltfield name="<i>name</i>" type="<i>type</i>"/&gt</code> element
	 * as child to <code>&ltoutput-structure&gt</code> element.
	 * 
	 * @param name name of output structure field
	 * @param type type of output structure field
	 * @throws VirtualSensorException if <code>&ltprocessing-class&gt</code> element doesn't exist 
	 */
	public void addOutputStructureField(String name, String type) throws VirtualSensorException {
		if (processingClass == null)
			throw new VirtualSensorException(
					"You must set processing class before output-structure.");
		if (outputStructure == null) {
			outputStructure = virtualSensor
					.createElement("output-structure");
			processingClass.appendChild(outputStructure);
		}

		Element element = virtualSensor.createElement("field");
		element.setAttribute("name", name);
		element.setAttribute("type", type);// implementirati provjeru za "type"
		outputStructure.appendChild(element);
	}
	
	/**
	 * Adds <code>&ltfield name="<i>name</i>" type="<i>type</i>"&gt<i>description</i>&ltfield&gt</code> element
	 * as child to <code>&ltoutput-structure&gt</code> element.
	 * 
	 * @param name name of output structure field
	 * @param type type of output structure field
	 * @param description short description of <code>&ltfield&gt</code> element
	 * @throws VirtualSensorException if <code>&ltprocessing-class&gt</code> element doesn't exist
	 */
	public void addOutputStructureField(String name, String type, String description) throws VirtualSensorException {
		if (processingClass == null)
			throw new VirtualSensorException(
					"You must set processing class before output-structure.");
		if (outputStructure == null) {
			outputStructure = virtualSensor
					.createElement("output-structure");
			processingClass.appendChild(outputStructure);
		}

		Element element = virtualSensor.createElement("field");
		element.setAttribute("name", name);
		element.setAttribute("type", type);// implementirati provjeru za
							// "type"
		element.setTextContent(description);
		outputStructure.appendChild(element);
	}
	
	/**
	 * Removes <code>&ltfield&gt</code> element with given <code>name</code> attribute.
	 * 
	 * @param name value of <code>name</code> attribute 
	 */
	public void removeOutputStructureField(String name) {
		if(outputStructure != null) {
			NodeList children = outputStructure.getChildNodes();
			Element element = null;
			for (int i = 0; i < children.getLength(); i++) {
				element = (Element)children.item(i);
				if(element.getAttribute("name").equals(name)) {
					outputStructure.removeChild(element);
					break;
				}
			}
		}
	}
	
	/**
	 * Returns value of <code>type</code> attribute from <code>&ltfield&gt</code> element
	 * with given <code>name</code> attribute.
	 * 
	 * @param name value of <code>name</code> attribute
	 * @return value of <code>type</code> attribute if field exists <code>null</code> otherwise
	 * @throws VirtualSensorException if <code>&ltprocessing-class&gt</code> element doesn't exist 
	 */
	public String getOutputStructureFieldType(String name) throws VirtualSensorException {
		if (processingClass == null)
			throw new VirtualSensorException(
					"You must set processing class before output-structure.");
		if(outputStructure != null) {
			NodeList children = outputStructure.getChildNodes();
			Element element = null;
			for (int i = 0; i < children.getLength(); i++) {
				element = (Element)children.item(i);
				if(element.getAttribute("name").equals(name)) {
					return element.getAttribute("type");
				}
			}
		}
		return null;
	}

	// OUTPUT-SPECIFICATION

	/**
	 * Sets value of <code>rate</code> attribute in <code>&ltoutput-specification&gt</code> element.
	 * 
	 * @param rate value of <code>rate</code> attribute
	 * @throws VirtualSensorException if <code>&ltprocessing-class&gt</code> element doesn't exist
	 */
	public void setOutputSpecificationRate(int rate) throws VirtualSensorException {
		if (processingClass == null)
			throw new VirtualSensorException(
					"You must set processing class before output-specification.");
		if (outputSpecifications == null) {
			outputSpecifications = virtualSensor
					.createElement("output-specification");
			processingClass.appendChild(outputSpecifications);
		}
		outputSpecifications.setAttribute("rate", String.valueOf(rate));
	}
	
	/**
	 * Returns value of <code>rate</code> in <code>&ltoutput-specification&gt</code> element.
	 * 
	 * @return value of <code>rate</code> attribute if attribute exists <code>null</code> otherwise
	 * @throws VirtualSensorException if <code>&ltprocessing-class&gt</code> element doesn't exist
	 */
	public String getOutputSpecificationRate() throws VirtualSensorException {
		if (processingClass == null)
			throw new VirtualSensorException(
					"You must set processing class before output-specification.");
		if(outputSpecifications != null) {
			return outputSpecifications.getAttribute("rate");
		}
		return null;
	}

	// WEB-INPUT

	/**
	 * Adds <code>&ltweb-input password="<i>password</i>"<>
	 * 
	 * @throws VirtualSensorException if <code>&ltprocessing-class&gt</code> element doesn't exist
	 */
	public void addWebInput() throws VirtualSensorException {
		if (processingClass == null)
			throw new VirtualSensorException(
					"You must set processing class before output-specification.");
		if (webInput == null)
			webInput = virtualSensor.createElement("web-input");
	}

	public void addWebInput(String password) throws VirtualSensorException {
		addWebInput();
		webInput.setAttribute("password", password);
	}

	public void addWebInputCommand(String name) {
		if (webInput == null)
			webInput = virtualSensor.createElement("web-input");
		Element command = virtualSensor.createElement("command");
		command.setAttribute("name", name);
		webInput.appendChild(command);
	}

	public void addCommandField(String commandName, String fieldName, String fieldType, String fieldDescription) throws VirtualSensorException {
		Element command = getCommand(commandName);
		if (command == null)
			throw new VirtualSensorException("Command "
					+ commandName + " doesn't exist.");

		Element field = virtualSensor.createElement("field");
		field.setAttribute("name", fieldName);
		field.setAttribute("type", fieldType);
		field.setTextContent(fieldDescription);

		command.appendChild(field);
	}

	private Element getCommand(String commandName) throws VirtualSensorException {
		if (webInput == null)
			throw new VirtualSensorException(
					"Web-input doesn't exist.");

		NodeList list = webInput.getChildNodes();
		Element command = null;
		for (int i = 0; i < list.getLength(); i++) {
			command = (Element) list.item(i);
			if (command.getAttribute("name").equals(commandName)) {
				return command;
			}
		}
		return null;
	}

	// VS DESCRIPTION

	public void setVSDescription(String text) {
		if (description == null)
			description = virtualSensor
					.createElement("description");
		description.setTextContent(text);
	}
	
	public String getVSDescription() {
		if(description == null) {
			return null;
		} else {
			return description.getTextContent();
		}
	}

	// LIFE-CYCLE

	/**
	 * Sets <code>pool-size</code> attribute of <code>&ltlife-cycle&gt<code> element.
	 * Life-cycle is deprecated and not used by GSN anymore.
	 * 
	 * @param poolSize
	 */
	@Deprecated
	public void setPoolSize(int poolSize) {
		if (lifeCycle == null)
			lifeCycle = virtualSensor.createElement("life-cycle");
		lifeCycle.setAttribute("pool-size", String.valueOf(poolSize));
	}

	// ADDRESSING

	public void addAddressingPredicate(String key, String value) {
		Element element = virtualSensor.createElement("predicate");
		element.setAttribute("key", key);
		element.setTextContent(value);
		addressing.appendChild(element);
	}

	// STORAGE

	public void setHistorySize(String historySize) {
		if (storage == null) storage = virtualSensor.createElement("storage");
		storage.setAttribute("history-size", historySize);
	}
	
	public void setDriver(String driver) {
		if (storage == null) storage = virtualSensor.createElement("storage");
		storage.setAttribute("driver", driver);
	}
	
	public void setUser(String user) {
		if (storage == null) storage = virtualSensor.createElement("storage");
		storage.setAttribute("user", user);
	}
	
	public void setPassword(String password) {
		if (storage == null) storage = virtualSensor.createElement("storage");
		storage.setAttribute("password", password);
	}
	
	public void setUrl(String url) {
		if (storage == null) storage = virtualSensor.createElement("storage");
		storage.setAttribute("url", url);
	}
	
	public void setIdentifier(String identifier) {
		if (storage == null) storage = virtualSensor.createElement("storage");
		storage.setAttribute("identifier", identifier);
	}

	// STREAMS

	public void addStream(String streamName) {
		Element stream = virtualSensor.createElement("stream");
		stream.setAttribute("name", streamName);
		streams.appendChild(stream);// if same stream exists, it will be overwritten
		
	}

	public void removeStream(String streamName) {
		Element stream = getStream(streamName);
		if (stream != null)
			streams.removeChild(stream);
	}

	public void setCount(String streamName, int count) throws VirtualSensorException {
		Element stream = getStream(streamName);
		if (stream != null) {
			stream.setAttribute("count", String.valueOf(count));
		} else {
			throw new VirtualSensorException("Stream " + streamName
					+ " doesn't exist.");
		}
	}

	public void setRate(String streamName, int rate) throws VirtualSensorException {
		Element stream = getStream(streamName);
		if (stream != null) {
			stream.setAttribute("rate", String.valueOf(rate));
		} else {
			throw new VirtualSensorException("Stream " + streamName
					+ " doesn't exist.");
		}
	}

	// SOURCE

	public void addSource(String streamName, String sourceAlias) throws VirtualSensorException {
		Element stream = getStream(streamName);
		if (stream == null) throw new VirtualSensorException("Stream " + streamName + " doesn't exist.");

		Element source = virtualSensor.createElement("source");
		source.setAttribute("alias", sourceAlias);
		stream.appendChild(source);// if stream already exists, it will be overwritten
	}

	public void setDisconnectBufferSize(String streamName, String sourceAlias, String value) throws VirtualSensorException {
		Element source = getSource(streamName, sourceAlias);
		if (source == null)
			throw new VirtualSensorException("Source "
					+ sourceAlias + " doesn't exist.");
		source.setAttribute("disconnect-buffer-size", value);
	}

	public void setStorageSize(String streamName, String sourceAlias, int storageSize) throws VirtualSensorException {
		Element source = getSource(streamName, sourceAlias);
		if (source == null)
			throw new VirtualSensorException("Source "
					+ sourceAlias + " doesn't exist.");
		source.setAttribute("storage-size", String.valueOf(storageSize));
	}

	public void setSlide(String streamName, String sourceAlias, int slide) throws VirtualSensorException {
		Element source = getSource(streamName, sourceAlias);
		if (source == null)
			throw new VirtualSensorException("Source "
					+ sourceAlias + " doesn't exist.");
		source.setAttribute("storage-size", String.valueOf(slide));
	}

	public void setSamplingRate(String streamName, String sourceAlias, float samplingRate) throws VirtualSensorException {
		Element source = getSource(streamName, sourceAlias);
		if (source == null)
			throw new VirtualSensorException("Source "
					+ sourceAlias + " doesn't exist.");

		float acceptableSamplingRate;
		if (samplingRate < 0) {
			acceptableSamplingRate = 0;
		} else if (samplingRate > 1) {
			acceptableSamplingRate = 1;
		} else {
			acceptableSamplingRate = samplingRate;
		}

		source.setAttribute("sampling-rate",
				String.valueOf(acceptableSamplingRate));
	}
	
	private Element getSource(String streamName, String sourceAlias) throws VirtualSensorException {
		Element stream = getStream(streamName);
		if (stream == null)
			throw new VirtualSensorException("Stream " + streamName
					+ " doesn't exist.");

		NodeList list = stream.getChildNodes();
		Element element = null;
		for (int i = 0; i < list.getLength(); i++) {
			element = (Element) list.item(i);
			if (element.getAttribute("alias").equals(sourceAlias)) {
				return element;
			}
		}
		return null;
	}
	
	private Element getStream(String streamName) {
		NodeList list = streams.getChildNodes();
		Element element = null;
		for (int i = 0; i < list.getLength(); i++) {
			element = (Element) list.item(i);
			if (element.getAttribute("name").equals(streamName)) {
				return element;
			}
		}
		return null;
	}

	// ADDRESS(WRAPPER)

	public void addAddress(String streamName, String sourceAlias, String wrapper) throws VirtualSensorException {
		Element source = getSource(streamName, sourceAlias);
		if (source == null)
			throw new VirtualSensorException("Source "
					+ sourceAlias + " doesn't exist.");

		Element address = virtualSensor.createElement("address");
		address.setAttribute("wrapper", wrapper);
		source.appendChild(address);// if address already exists, it
						// will be overwritten
	}

	public void addAddressPredicate(String streamName, String sourceAlias, String wrapper, String key, String value) throws VirtualSensorException {
		Element address = getAddress(streamName, sourceAlias, wrapper);
		if (address == null)
			throw new VirtualSensorException(
					"Address for wrapper: " + wrapper
							+ " doesn't exist.");

		Element predicate = virtualSensor.createElement("predicate");
		predicate.setAttribute("key", key);
		predicate.setTextContent(value);
		address.appendChild(predicate);// if predicate already exists,
						// it will be overwritten
	}
	
	private Element getAddress(String streamName, String sourceAlias, String wrapper) throws VirtualSensorException {
		Element source = getSource(streamName, sourceAlias);
		if (source == null)
			throw new VirtualSensorException("Source "
					+ sourceAlias + " doesn't exist.");

		NodeList list = source.getChildNodes();
		Element element = null;
		for (int i = 0; i < list.getLength(); i++) {
			element = (Element) list.item(i);
			if (element.getAttribute("wrapper").equals(wrapper)) {
				return element;
			}
		}
		return null;
	}

	// SOURCE QUERY

	public void setSourceQuery(String streamName, String sourceAlias, String query) throws VirtualSensorException {
		Element source = getSource(streamName, sourceAlias);
		if (source == null)
			throw new VirtualSensorException("Source "
					+ sourceAlias + " doesn't exist.");

		Element queryElement = (Element) source.getElementsByTagName("query");
		if (queryElement == null) {
			queryElement = virtualSensor.createElement("query");
			queryElement.setTextContent(query);
			source.appendChild(queryElement);
		} else {
			queryElement.setTextContent(query);
		}
	}

	// STREAM QUERY

	public void setStreamQuery(String streamName, String query) throws VirtualSensorException {
		Element stream = getStream(streamName);
		if (stream == null)
			throw new VirtualSensorException("Stream " + streamName
					+ " doesn't exist.");

		Element queryElement = null;
		Element tmp = null;
		NodeList childNodes = stream.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			tmp = (Element)childNodes.item(i);
			if(tmp.getNodeName().equals("query")) {
				queryElement = tmp;
			}
		}

		if (queryElement == null) {
			queryElement = virtualSensor.createElement("query");
			queryElement.setTextContent(query);
			stream.appendChild(queryElement);
		} else {
			queryElement.setTextContent(query);
		}
	}

	// OUTPUT CREATED XML

	public void writeToFile(File file) throws TransformerException,	VirtualSensorException {
		checkIfDocumentIsValid();
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		DOMSource source = new DOMSource(virtualSensor);
		StreamResult result = new StreamResult(file);

		//don't change format of existing file(that file is owned by client)
		if(!fileUploaded) {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		}
		
		transformer.transform(source, result);
	}

	private void checkIfDocumentIsValid() {
		StringBuilder sb = new StringBuilder();

		if (!rootElement.hasAttribute("name")) sb.append("Virtual sensor name is not specified.");
		if (!(sb.length() == 0)) throw new VirtualSensorException(sb.toString());

		// ADD DEFAULT QUERYS AND/OR REMOVE UNNECESSARY ONES
		if(streams.hasChildNodes()) {
			NodeList list = streams.getChildNodes();
			Element stream = null;
			NodeList tmp = null;
			for (int i = 0; i < list.getLength(); i++) {
				stream = (Element)list.item(i);
				if(!stream.hasChildNodes()) continue;
				
				tmp = stream.getChildNodes();
				
				StringBuilder sb2 = new StringBuilder();
				Element query = null;
				Element source = null;
				boolean hasWrapper = false;
				for (int j = 0; j < tmp.getLength(); j++) {
					source = (Element)tmp.item(j);
					sb2.append(source.getAttribute("alias")).append(", ");
					if(!source.hasChildNodes()) continue;
					
					if(source.getTagName().equals("source")) {
						if(source.getElementsByTagName("address").getLength() == 0) {
							NodeList lst = source.getElementsByTagName("query");
							if(lst.getLength() > 0) {
								source.removeChild(lst.item(0));//there is only one "query" tag for sure
							}
							continue;
						}
						
						hasWrapper = true;
						NodeList tmp2 = source.getElementsByTagName("query");
						if(tmp2.getLength() == 0) {
							query = virtualSensor.createElement("query");
							query.setTextContent("SELECT * FROM WRAPPER");
							source.appendChild(query);
						}
					}
				}
	
				if(!hasWrapper) {
					tmp = stream.getElementsByTagName("query");
					if(tmp.getLength() > 0) {
						for (int j = 0; j < tmp.getLength(); j++) {
							stream.removeChild(tmp.item(j));
						}
					}
					continue;
				}
				
				tmp = stream.getChildNodes();
				boolean hasQuery = false;
				for (int j = 0; j < tmp.getLength(); j++) {
					if(tmp.item(j).getNodeName().equals("query")) {
						hasQuery = true;
					}
				}
				if(!hasQuery) {
					sb2.delete(sb2.length() - 2, sb2.length());
					query = virtualSensor.createElement("query");
					query.setTextContent("SELECT * FROM " + sb2.toString());
					stream.appendChild(query);
				}
			}
		}
		
		//if this is not new VSD, root element already has this elements binded to itself
		if(!fileUploaded) {
			if (processingClass != null) rootElement.appendChild(processingClass);
			if (description != null) rootElement.appendChild(description);
			if (lifeCycle != null) rootElement.appendChild(lifeCycle);
			rootElement.appendChild(addressing);
			rootElement.appendChild(storage);
			rootElement.appendChild(streams);
		}
	}
}
