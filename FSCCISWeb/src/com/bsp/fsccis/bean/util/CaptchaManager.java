package com.bsp.fsccis.bean.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named("captchaManager")
public class CaptchaManager {
	private static Integer captchaLength = 6;
	private String captchatext;

	public StreamedContent getCaptchaImage() throws IOException {
		int width = 200;
		int height = 70;

		Integer[] i = getDimension();
		int x = i[0];
		int y = i[1];

		Font font = getFont();
		Color fg = getBgColor();
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g1d = image.createGraphics();
		setRenderingHints(g1d);
		TextLayout textLayout = new TextLayout(generateCaptchaText(), font,
				g1d.getFontRenderContext());
		BufferedImage img = null;
		img = ImageIO.read(this.getClass().getResource(getBgImage()));
		
		g1d.drawImage(img, 0, 0, width, height, 0, 0, width, height, null);  
		g1d.setPaint(fg);
		textLayout.draw(g1d, x + 3, y + 3);
		g1d.dispose();
		float[] kernel = { 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f,
				1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f };

		ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernel),
				ConvolveOp.EDGE_NO_OP, null);
		BufferedImage image2 = op.filter(image, null);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Graphics2D g2d = image2.createGraphics();
				
		setRenderingHints(g2d);
		
		g2d.setPaint(Color.BLACK);
		textLayout.draw(g2d, x, y);
		
		g2d.dispose();
		
		try {
			ImageIO.write(image2, "png", os);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, Object> sessionMap = context.getExternalContext()
				.getSessionMap();
		sessionMap.put("captchatext", captchatext);
		//return new DefaultStreamedContent(new ByteArrayInputStream(
		//		os.toByteArray()), "image/png");
		new DefaultStreamedContent();
		return DefaultStreamedContent.builder()
				.contentType("image/png")
                .stream(() -> new ByteArrayInputStream(os.toByteArray()))
                .build();

	}

	private String getBgImage() {
		Random rn = new Random();
		List<String> ii = new ArrayList<String>();
		ii.add("red.jpeg");
		ii.add("green.jpeg");
		ii.add("black.jpeg");
		
		Integer max = ii.size() - 1;
		Integer alignRnd = rn.nextInt(max - 0 + 1) + 0;

		String c = ii.get(alignRnd);
		
		
		return "/resources/images/captchaBg/" + c;
	}

	private void setRenderingHints(Graphics2D g) {

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Font getFont() {
		Random rn = new Random();
		List<Font> ii = new ArrayList<Font>();
		ii.add(new Font(getFontFamily(), getFontWeight(), getFontSize()));
		ii.add(new Font(getFontFamily(), getFontWeight(), getFontSize()));
		ii.add(new Font(getFontFamily(), getFontWeight(), getFontSize()));
		Integer alignRnd = rn.nextInt(2 - 0 + 1) + 0;

		Font c = ii.get(alignRnd);
		Map attributes = c.getAttributes();
		attributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
		Font newFont = new Font(attributes);
		return newFont;
	}

	private int getFontSize() {
		Random rn = new Random();
		Integer alignRnd = rn.nextInt(25 - 20 + 1) + 20;
		return alignRnd;
	}

	private String getFontFamily() {
		Random rn = new Random();
		List<String> ii = new ArrayList<String>();
		ii.add("Verdana");
		ii.add("Book Antiqua");
		ii.add("Tahoma");
		
		Integer max = ii.size() - 1;
		Integer alignRnd = rn.nextInt(max - 0 + 1) + 0;

		String c = ii.get(alignRnd);
		return c;
	}

	private Integer getFontWeight() {
		Random rn = new Random();
		List<Integer> ii = new ArrayList<Integer>();
		ii.add(Font.ITALIC);
		ii.add(Font.BOLD);
		ii.add(Font.PLAIN);
		
		Integer max = ii.size() - 1;
		Integer alignRnd = rn.nextInt(max - 0 + 1) + 0;

		Integer c = ii.get(alignRnd);
		return c;
	}

	private Color getBgColor() {

		Random rn = new Random();
		List<Color> ii = new ArrayList<Color>();
		ii.add(new Color(216, 42, 18));
		ii.add(new Color(22, 28, 142));
		ii.add(new Color(23, 198, 23));
		
		Integer max = ii.size() - 1;
		Integer alignRnd = rn.nextInt(max - 0 + 1) + 0;

		Color c = ii.get(alignRnd);
		return c;
	}

	private Integer[] getDimension() {
		Random rn = new Random();

		List<Integer[]> ii = new ArrayList<Integer[]>();
		ii.add(new Integer[] { 10, 25 });
		ii.add(new Integer[] { 100, 55 });
		ii.add(new Integer[] { 10, 55 });
		ii.add(new Integer[] { 35, 35 });
		
		Integer max = ii.size() - 1;
		Integer alignRnd = rn.nextInt(max - 0 + 1) + 0;

		return ii.get(alignRnd);

	}

	private String generateCaptchaText() {

		String saltChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuffer captchaStrBuffer = new StringBuffer();
		java.util.Random rnd = new java.util.Random();

		// build a random captchaLength chars
		while (captchaStrBuffer.length() < captchaLength) {
			int index = (int) (rnd.nextFloat() * saltChars.length());
			captchaStrBuffer.append(saltChars.substring(index, index + 1));
		}

		captchatext = captchaStrBuffer.toString();
		return captchaStrBuffer.toString();

	}

	public static Boolean validate(Map<String, Object> sessionMap,
			String jcaptcha) {
		return sessionMap.get("captchatext").equals(jcaptcha);
	}

}
