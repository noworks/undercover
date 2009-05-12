package undercover.instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import undercover.metric.ClassMetric;
import undercover.metric.MetaData;

public class Instrument {
	public void instrumentDirs(File[] inputDirs, File outputDir) {
		outputDir.mkdirs();
		for (File each : inputDirs) {
			instrumentDir(each, outputDir);
		}
	}
	
	public void instrumentDir(File inputDir, File outputDir) {
		outputDir.mkdir();
		for (File each : inputDir.listFiles()) {
			if (each.isDirectory()) {
				instrumentDir(each, new File(outputDir, each.getName()));
			} else if (each.getName().endsWith(".class")) {
				instrumentFile(each, new File(outputDir, each.getName()));
			} else {
				copyFile(each, new File(outputDir, each.getName()));
			}
		}
	}

	void copyFile(File inputFile, File outputFile) {
		try {
			FileUtils.copyFile(inputFile, outputFile);
		} catch (IOException e) {
			throw new InstrumentException("Cannot copy " + inputFile.getAbsolutePath() + " to " + outputFile.getAbsoluteFile(), e);
		}
	}

	public void instrumentFile(File inputFile, File outputFile) {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(inputFile);
			output = new FileOutputStream(outputFile);
			output.write(instrument(input));
		} catch (IOException e) {
			throw new InstrumentException("Cannot instrument " + inputFile.getAbsolutePath() + " to " + outputFile.getAbsoluteFile(), e);
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(input);
		}
	}
	
	public byte[] instrument(byte[] bytecode) {
		ClassReader reader = new ClassReader(bytecode);
		ClassWriter writer = instrument(reader);
		return writer.toByteArray();
	}
	
	public byte[] instrument(InputStream bytecode) throws IOException {
		ClassReader reader = new ClassReader(bytecode);
		ClassWriter writer = instrument(reader);
		return writer.toByteArray();
	}
	
	public ClassWriter instrument(ClassReader classReader) {
		MetaData metaData = new MetaData(new ArrayList<ClassMetric>());
		ClassWriter classWriter = new ClassWriter(classReader, 0);
		classReader.accept(new InstrumentClass(classWriter, metaData), 0);
		System.out.println(metaData.toString());
		return classWriter;
	}
}
