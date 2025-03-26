package com.intuitivecare.testsJava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebScrapingTest {
	public static void main(String[] args) {
		String websiteUrl = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos/";
		String outputFolder = "./Resultados"; // Pasta onde os arquivos serão
																					// salvos
		String zipPath = outputFolder + "/Anexos.zip";

		try {
			// Garante que a pasta existe
			Files.createDirectories(Paths.get(outputFolder));

			// Busca os links de download do arquivo pdf no site
			List<String> pdfUrls = getPdfLinks(websiteUrl);

			String[] pdfFiles = new String[pdfUrls.size()];

			// Realiza download de cada arquivo e salva na pasta especificada em
			// outputFolder
			for (int i = 0; i < pdfUrls.size(); i++) {
				pdfFiles[i] = downloadFile(pdfUrls.get(i), outputFolder);
			}


			// Compacta os arquivos para um arquivo ZIP
			zipFiles(pdfFiles, zipPath);
			System.out.println("Arquivo " + zipPath + " criado com sucesso!");
			
			
			//Apaga os arquivos baixados depois que são compactados, evitando duplicidade
			deleteFile(pdfFiles);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Método para obter os links dos PDFs a partir da página da web
	public static List<String> getPdfLinks(String websiteUrl) throws IOException {
		Document doc = Jsoup.connect(websiteUrl).get();
		Elements links = doc.select("a[href$=.pdf]");		
		String[]  getLinks = links.stream().map(link -> link.absUrl("href")).toArray(String[]::new);
		ArrayList<String> urls = new ArrayList<String>();		
		for (int i = 0; i < getLinks.length; i++) {
			if(getLinks[i].contains(websiteUrl)) {
				urls.add(getLinks[i]);
			}
		}
		return urls;

	}

	// Método para baixar um arquivo PDF de um URL e salvá-lo na pasta especificada
	public static String downloadFile(String fileUrl, String outputFolder) throws IOException {
		String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
		String filePath = outputFolder + "/" + fileName;
		try (InputStream in = new URL(fileUrl).openStream()) {
			Files.copy(in, Paths.get(filePath));
		}
		return filePath;
	}

	// Método para compactar os arquivos PDF em um único arquivo ZIP
	public static void zipFiles(String[] files, String zipPath) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(zipPath); ZipOutputStream zos = new ZipOutputStream(fos)) {

			for (String file : files) {
				try (FileInputStream fis = new FileInputStream(file)) {
					ZipEntry zipEntry = new ZipEntry(new File(file).getName()); // Adiciona cada arquivo ao ZIP
					zos.putNextEntry(zipEntry);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = fis.read(buffer)) >= 0) {
						zos.write(buffer, 0, length);
					}
				}
			}
		}
	}

	public static void deleteFile(String[] files) {
		for (String file : files) {
			File fileToDelete = new File(file);
			fileToDelete.delete();
		}
	}
}
