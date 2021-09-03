package com.example.myplugin.util;



import com.example.myplugin.casper.entity.CallSite;
import com.example.myplugin.casper.entity.CallSiteCFG;
import com.example.myplugin.casper.entity.Method;
import com.example.myplugin.casper.entity.MethodInstrumentSolution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;


public class StoreMetaData {
	@SuppressWarnings("rawtypes")
	public void storeMetaData(String metaDataFile,
			HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions,
			HashMap<String, CallSiteCFG> callSiteCFGMap){
		try {
			OutputStream outputStream = new FileOutputStream(new File(
					metaDataFile));
			XMLStreamWriter out = XMLOutputFactory.newInstance()
					.createXMLStreamWriter(
							new OutputStreamWriter(outputStream, "utf-8"));
			out.writeStartDocument();
			out.writeStartElement("meta");
			out.writeCharacters("\r\n");
			
			
			
			
			// write the callsite info
			out.writeStartElement("Callsites");
			out.writeCharacters("\r\n");
			for (String method : callSiteCFGMap.keySet()) {
				CallSiteCFG callSiteCFG = callSiteCFGMap.get(method);
				if(callSiteCFG==null){
					continue;
				}
				for (CallSite callSite : callSiteCFG.getCallSites()) {
					out.writeStartElement("Callsite");
					out.writeCharacters("\r\n");					
					out.writeStartElement("CallSiteID");					
					out.writeCharacters( callSite.getCallSiteID()+"");
					out.writeEndElement();
					out.writeCharacters("\r\n");
					
					out.writeStartElement("CallerMethod");					
					out.writeCharacters( method);
					out.writeEndElement();
					out.writeCharacters("\r\n");
					
					if(callSite.isEntry()){
						out.writeStartElement("isEntry");						
						out.writeCharacters(true+"");
						out.writeEndElement();
						out.writeCharacters("\r\n");
					}else{
						out.writeStartElement("isEntry");
						out.writeCharacters(false+"");
						out.writeEndElement();
					}
					
					if(callSite.isExit()){
						out.writeStartElement("isExit");
						out.writeCharacters(true+"");
						out.writeEndElement();
						out.writeCharacters("\r\n");
					}else{
						out.writeStartElement("isExit");
						out.writeCharacters(false+"");
						out.writeEndElement();
						out.writeCharacters("\r\n");
					}
					
					
					out.writeStartElement("LineNum");
					out.writeCharacters( callSite.getLineNum() +"");
					out.writeEndElement();
					out.writeCharacters("\r\n");
					
					
					out.writeStartElement("BytecodeOffset");
					out.writeCharacters( callSite.getByteCodeOffset() +"");
					out.writeEndElement();
					out.writeCharacters("\r\n");
					
					out.writeStartElement("StmString");
					out.writeCharacters( callSite.getStmtString());
					out.writeEndElement();
					out.writeCharacters("\r\n");
					
					for(Method targetMethod:callSite.getPotentialCalleeMethods()){
						out.writeStartElement("TargetMethod");
						
						out.writeStartElement("TargetMethodName");
						out.writeCharacters(targetMethod.getMethodName());
						out.writeEndElement();
						out.writeCharacters("\r\n");
						
						out.writeStartElement("isAbstract");
						out.writeCharacters(targetMethod.isAbstract()+"");
						out.writeEndElement();
						out.writeCharacters("\r\n");
						
						out.writeStartElement("isLibrary");
						out.writeCharacters(targetMethod.isLibrary()+"");
						out.writeEndElement();
						out.writeCharacters("\r\n");
						
						out.writeStartElement("isNative");
						out.writeCharacters(targetMethod.isNative()+"");
						out.writeEndElement();
						out.writeCharacters("\r\n");
						
						out.writeEndElement(); //for TargetMethod
						out.writeCharacters("\r\n");						
					}
					
					out.writeEndElement(); // for Callsite
					out.writeCharacters("\r\n");
				}
			}
			out.writeEndElement(); // for Callsites
			
			
			// write the CFG info
			out.writeStartElement("CFGs");
			out.writeCharacters("\r\n");
			for (String method : callSiteCFGMap.keySet()) {
				CallSiteCFG callSiteCFG = callSiteCFGMap.get(method);
				if(callSiteCFG==null){
					continue;
				}				
				for (CallSite callSite : callSiteCFG.getCallSites()) {
					if (callSite.getSuccessors() != null) {
						for (CallSite successor : callSite.getSuccessors()) {
							out.writeStartElement("CFG");
							out.writeCharacters("\r\n");
							out.writeStartElement("SourceCallsite");
							out.writeCharacters(callSite.getCallSiteID() + "");
							out.writeEndElement();
							out.writeCharacters("\r\n");
							
							out.writeStartElement("TargetCallsite");
							out.writeCharacters(successor.getCallSiteID() + "");
							out.writeEndElement();
							out.writeCharacters("\r\n");
							out.writeEndElement(); // for CFG
							out.writeCharacters("\r\n");
						}
					}

				}
			}
			out.writeEndElement(); // for CFGs
			out.writeCharacters("\r\n");
			
			// write the solution
			out.writeStartElement("InstrumentSolution");
			for (String method : methodInstrumentSolutions.keySet()) {
				MethodInstrumentSolution solution = methodInstrumentSolutions
						.get(method);
				out.writeStartElement("MethodSolution");
				out.writeCharacters("\r\n");
				
				out.writeStartElement("method");
				out.writeCharacters(method + "");
				out.writeEndElement();
				out.writeCharacters("\r\n");
				
				for(CallSite callSite:solution.getInstrumentForLogging()){
					out.writeStartElement("CallSiteForLogging");
					out.writeCharacters(callSite.getCallSiteID() + "");
					out.writeEndElement();
					out.writeCharacters("\r\n");
				}
				
				
				for(CallSite callSite:solution.getInstrumentForPushingDepth()){
					out.writeStartElement("CallSiteForPushingDepth");
					out.writeCharacters(callSite.getCallSiteID() + "");
					out.writeEndElement();
					out.writeCharacters("\r\n");
				}
				
				for(CallSite callSite:solution.getInstrumentForPushingDepthAndID()){
					out.writeStartElement("CallSiteForPushingDepthAndID");
					out.writeCharacters(callSite.getCallSiteID() + "");
					out.writeEndElement();
					out.writeCharacters("\r\n");
				}
				
				out.writeEndElement(); // for MethodSolution
				out.writeCharacters("\r\n");
			}
			out.writeEndElement(); // for InstrumentSolution
			out.writeCharacters("\r\n");
			
			out.writeEndElement();// for meta
			out.writeCharacters("\r\n");
			out.writeEndDocument();
			out.writeCharacters("\r\n");
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
