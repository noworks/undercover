package undercover.report.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import undercover.report.Item;
import undercover.report.PackageItem;
import undercover.report.ReportData;
import undercover.report.ReportOutput;
import undercover.report.SourceItem;
import undercover.support.IOUtils;
import undercover.support.xml.DoctypeDeclaration;
import undercover.support.xml.Element;
import undercover.support.xml.XmlDeclaration;
import undercover.support.xml.XmlWriter;

public class HtmlReport {
	final String templateEncoding = "UTF-8";

	private ReportData reportData;
	private ReportOutput output;
	
	private StringTemplateGroup templateGroup;
	
	public HtmlReport() throws IOException {
		templateGroup = new StringTemplateGroup(new InputStreamReader(getClass().getResourceAsStream("default.stg"), templateEncoding), DefaultTemplateLexer.class);
		templateGroup.registerRenderer(String.class, new StringRenderer());
		templateGroup.registerRenderer(Double.class, new DoubleRenderer());
	}

	public void setReportData(ReportData reportData) {
		this.reportData = reportData;
	}

	public void setOutput(ReportOutput output) {
		this.output = output;
	}
	
	public void generate() throws IOException {
		copyResources();
		generateProjectReport();
		generatePackageReports();
		generateSourceReports();
		generateDashboardReport();
	}

	void copyResources() throws IOException {
		final String[] resources = {
				"index.html",
				"style.css",
				"jquery-1.3.2.min.js",
				"jquery.flot.pack.js",
				"excanvas.pack.js",
				"undercover.js",
		};
		for (String each : resources) {
			copyResource("resources/" + each, each);
		}
	}

	void copyResource(String sourcePath, String destPath) throws IOException {
		InputStream input = null;
		try {
			input = getClass().getResourceAsStream(sourcePath);
			output.write(destPath, input);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}
	
	void write(Element root, String path) throws IOException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output.openWriter(path));
			XmlWriter xmlWriter = new XmlWriter(writer);
			xmlWriter.visitXmlDeclaration(new XmlDeclaration("1.0", "UTF-8"));
			xmlWriter.visitDoctypeDeclaration(new DoctypeDeclaration("html", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", "-//W3C//DTD XHTML 1.0 Transitional//EN"));
			root.accept(xmlWriter);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	void generateProjectReport() throws IOException {
		generateProjectPackages();
		generateProjectSummary();
		generateProjectClasses();
	}
	
	void generateProjectPackages() throws IOException {
		if (false) {
			StringTemplate template = getTemplate("projectPackages");
			template.setAttribute("project", reportData);
			output.write("project-packages.html", template.toString());
		} else {
			write(new MenuPage(reportData).build(), "project-packages.html");
		}
	}

	void generateProjectSummary() throws IOException {
		if (false) {
			StringTemplate template = getTemplate("projectSummary");
			template.setAttribute("project", reportData);
			output.write("project-summary.html", template.toString());
		} else {
			write(new ProjectSummaryPage(reportData).build(), "project-summary.html");
		}
	}

	void generateProjectClasses() throws IOException {
		if (false) {
			StringTemplate template = getTemplate("projectClasses");
			template.setAttribute("classes", reportData.getClasses());
			output.write("project-classes.html", template.toString());
		} else {
			write(new ProjectClassListPage(reportData).build(), "project-classes.html");
		}
	}

	void generatePackageReports() throws IOException {
		for (PackageItem each : reportData.getPackages()) {
			generatePackageSummary(each);
			generatePackageClasses(each);
		}
	}

	void generatePackageSummary(PackageItem packageItem) throws IOException {
		if (false) {
			StringTemplate template = getTemplate("packageSummary");
			template.setAttribute("package", packageItem);
			output.write("package-" + packageItem.getLinkName() + "-summary.html", template.toString());
		} else {
			write(new PackageSummaryPage(packageItem).build(), "package-" + packageItem.getLinkName() + "-summary.html");
		}
	}

	void generatePackageClasses(PackageItem packageItem) throws IOException {
		if (false) {
			StringTemplate template = getTemplate("projectClasses");
			template.setAttribute("classes", packageItem.classes);
			output.write("package-" + packageItem.getLinkName() + "-classes.html", template.toString());
		} else {
			write(new PackageClassListPage(packageItem).build(), "package-" + packageItem.getLinkName() + "-classes.html");
		}
	}

	void generateSourceReports() throws IOException {
		for (SourceItem each : reportData.getSources()) {
			generateSourceSummary(each);
		}
	}
	
	void generateSourceSummary(SourceItem sourceItem) throws IOException {
		if (false) {
			StringTemplate st = getTemplate("sourceSummary");
			st.setAttribute("source", sourceItem);
			output.write("source-" + sourceItem.getLinkName() + ".html", st.toString());
		} else {
			write(new SourceSummaryPage(sourceItem).build(), "source-" + sourceItem.getLinkName() + ".html");
		}
	}
	
	void generateDashboardReport() throws IOException {
		if (false) {
			StringTemplate template = getTemplate("dashboard");
			template.setAttribute("project", reportData);
			CoverageDistribution coverageDistribution = new CoverageDistribution(reportData.getClasses());
			template.setAttribute("coverageDistribution", coverageDistribution);
			CoverageComplexity coverageComplexity = new CoverageComplexity(reportData.getClasses());
			template.setAttribute("coverageComplexity", coverageComplexity);
			template.setAttribute("mostRiskyClasses", mostRisky(reportData.getClasses(), 20));
			template.setAttribute("mostComplexClasses", mostComplex(reportData.getClasses(), 10));
			template.setAttribute("leastCoveredClasses", leastCovered(reportData.getClasses(), 10));
			output.write("project-dashboard.html", template.toString());
		} else {
			write(new DashboardPage(reportData).build(), "project-dashboard.html");
		}
	}

	<T extends Item> List<T> takeTopN(Collection<T> candidates, Comparator<T> comparator, int max) {
		List<T> items = new ArrayList<T>(candidates);
		Collections.sort(items, comparator);
		if (items.size() > max) {
			items = items.subList(0, max);
		}
		return items;
	}

	public <T extends Item> List<T> mostRisky(Collection<T> candidates, int max) {
		return takeTopN(candidates, new Comparator<T>() {
			public int compare(T a, T b) {
				return (int) Math.signum((b.getBlockMetrics().getRisk() - a.getBlockMetrics().getRisk()));
			}
		}, max);
	}

	public <T extends Item> List<T> mostComplex(Collection<T> candidates, int max) {
		return takeTopN(candidates, new Comparator<T>() {
			public int compare(T a, T b) {
				return b.getBlockMetrics().getComplexity() - a.getBlockMetrics().getComplexity();
			}
		}, max);
	}

	public <T extends Item> List<T> leastCovered(Collection<T> candidates, int max) {
		return takeTopN(candidates, new Comparator<T>() {
			public int compare(T a, T b) {
				return (int) Math.signum(a.getBlockMetrics().getCoverage().getRatio() - b.getBlockMetrics().getCoverage().getRatio());
			}
		}, max);
	}

	public StringTemplate getTemplate(String templateName) {
		return templateGroup.getInstanceOf(templateName);
	}
}
