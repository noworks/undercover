package undercover.ant;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import undercover.data.CoverageData;
import undercover.data.MetaData;
import undercover.report.ReportData;
import undercover.report.ReportDataBuilder;
import undercover.report.ReportOutput;
import undercover.report.SourceFinder;
import undercover.report.html.HtmlReport;
import undercover.report.xml.CoberturaXmlReport;
import undercover.report.xml.EmmaXmlReport;

public class ReportTask extends UndercoverTask {
	Path sourcePath;
	String sourceEncoding;
	List<ReportFormat> formats = new ArrayList<ReportFormat>();
	
	List<File> sourcePaths;
	
	/**
	 * sourcepath element.
	 */
	public Path createSourcePath() {
		if (sourcePath == null) {
			sourcePath = new Path(getProject());
		}
		return sourcePath.createPath();
	}
	
	public void setSourceEncoding(String sourceEncoding) {
		this.sourceEncoding = sourceEncoding;
	}
	
	public ReportFormat createHtml() {
		ReportFormat format = new HtmlFormat();
		formats.add(format);
		return format;
	}
	
	public ReportFormat createCoberturaXml() {
		ReportFormat format = new CoberturaXmlFormat();
		formats.add(format);
		return format;
	}
	
	public ReportFormat createEmmaXml() {
		ReportFormat format = new EmmaXmlFormat();
		formats.add(format);
		return format;
	}	
	
	void checkParameters() {
		checkMetaDataFile();
		checkCoverageDataFile();
		checkSourcePath();
    	checkSourceEncoding();
	}

	void checkSourcePath() {
    	sourcePaths = new ArrayList<File>();
    	if (sourcePath != null) {
	    	for (String each : sourcePath.list()) {
	    		sourcePaths.add(new File(each));
	    	}
    	}
		log("Source path: " + sourcePaths);
	}
	
	void checkSourceEncoding() {
		if (sourceEncoding == null) {
			sourceEncoding = Charset.defaultCharset().name();
		}
	}

    public void execute() throws BuildException {
    	log("Reporting...");
    	checkParameters();
    	
		if (!metaDataFile.exists()) {
			log("Meta data is not exist", Project.MSG_WARN);
			return;
		}

		SourceFinder sourceFinder = new SourceFinder(sourcePaths, sourceEncoding);
		try {
			MetaData metaData = MetaData.load(metaDataFile);
			CoverageData coverageData = coverageDataFile.exists() ? CoverageData.load(coverageDataFile) : new CoverageData();
			ReportDataBuilder builder = new ReportDataBuilder(coverageData);
			builder.setProjectName(getProject().getName());
			builder.setSourceFinder(sourceFinder);
			metaData.accept(builder);
			ReportData reportData = builder.getReportData();
			
			for (ReportFormat each : formats) {
				each.setReportData(reportData);
				each.generate();
			}
		} catch (IOException e) {
			throw new BuildException("Failed to generate report", e);
		}
    }
    
    public static abstract class ReportFormat {
		protected ReportData reportData;
    	protected File output;
    	protected String encoding;
    	
    	public void setReportData(ReportData reportData) {
    		this.reportData = reportData;
    	}
    	
    	public void setOutput(File output) {
    		this.output = output;
    	}
    	
    	public void setEncoding(String encoding) {
    		this.encoding = encoding;
    	}
    		
    	public abstract void generate() throws IOException ;
    }
    
    public static class HtmlFormat extends ReportFormat {
		public void generate() throws IOException {
			checkEncoding();
			HtmlReport report = new HtmlReport();
			report.setReportData(reportData);
			report.setOutput(new ReportOutput(output, encoding));
			report.generate();
		}
		
		void checkEncoding() {
			if (encoding == null) {
				encoding = "UTF-8";
			}
		}
    }

    public static class CoberturaXmlFormat extends ReportFormat {
		public void generate() throws IOException {
			checkEncoding();
			CoberturaXmlReport report = new CoberturaXmlReport();
			report.setReportData(reportData);
			report.setOutput(output);
			report.generate();
		}
		
		void checkEncoding() {
			if (encoding == null) {
				encoding = "UTF-8";
			}
		}
    }

    public static class EmmaXmlFormat extends ReportFormat {
		public void generate() throws IOException {
			checkEncoding();
			
			new EmmaXmlReport().with(reportData).writeTo(output, encoding);
		}
		
		void checkEncoding() {
			if (encoding == null) {
				encoding = "UTF-8";
			}
		}
    }
}
