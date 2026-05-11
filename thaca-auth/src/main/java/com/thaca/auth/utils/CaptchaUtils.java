package com.thaca.auth.utils;

import com.thaca.auth.dtos.CaptchaDTO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import javax.imageio.ImageIO;
import lombok.experimental.UtilityClass;

/**
 * ColorfulCaptcha — cartoon-style colorful CAPTCHA, no external libs.
 *
 * Three modes:
 * TEXT — mixed upper+lowercase letters + digits e.g. "aB3Kp"
 * MATH — arithmetic expression e.g. "6+8-1=?" (answer stored in Result)
 * AUTO — randomly picks TEXT or MATH
 *
 * Usage:
 * // Text captcha
 * ColorfulCaptcha.Result r = ColorfulCaptcha.generateText(5);
 * String answer = r.answer; // "aB3Kp"
 * String b64 = r.base64;
 *
 * // Math captcha
 * ColorfulCaptcha.Result r = ColorfulCaptcha.generateMath();
 * String answer = r.answer; // "13" (numeric answer as string)
 * String b64 = r.base64;
 *
 * // HTML: <img src="data:image/png;base64,{b64}" />
 */
@UtilityClass
public class CaptchaUtils {

    // ── image size ────────────────────────────────────────────────────────────
    private static final int IMG_W = 240;
    private static final int IMG_H = 75;

    // ── character pools ───────────────────────────────────────────────────────
    // Exclude visually ambiguous: 0/O, 1/l/I, 5/S
    private static final String UPPER = "ABCDEFGHJKMNPQRTUVWXYZ";
    private static final String LOWER = "abcdefghjkmnpqrtuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String MIXED = UPPER + LOWER + DIGITS;

    // ── vivid palette ─────────────────────────────────────────────────────────
    private static final Color[] COLORS = {
        new Color(0xFF, 0x45, 0x00), // orange-red
        new Color(0x00, 0x99, 0xFF), // electric blue
        new Color(0xFF, 0xBB, 0x00), // golden yellow
        new Color(0x22, 0xBB, 0x44), // fresh green
        new Color(0xFF, 0x33, 0x99), // hot pink
        new Color(0x88, 0x22, 0xFF), // violet
        new Color(0xFF, 0x22, 0x22), // red
        new Color(0x00, 0xBB, 0xAA), // teal
        new Color(0xFF, 0x77, 0x00), // amber
        new Color(0x33, 0x99, 0x00) // grass green
    };

    // ── math operators (× = Unicode multiply sign) ───────────────────────────
    private static final char[] OPS = { '+', '-', '\u00D7' };

    // ── font families (Java built-in logical fonts only) ──────────────────────
    private static final String[] FONTS = { Font.SERIF, Font.SANS_SERIF, "Dialog", "Monospaced" };

    private static final String DATA_URI_PREFIX = "data:image/png;base64,";

    /** Mixed upper/lowercase + digits CAPTCHA. */
    public static CaptchaDTO generateText(int length) throws Exception {
        String code = randomText(length);
        return CaptchaDTO.builder().image(DATA_URI_PREFIX + toBase64(renderText(code))).answer(code).build();
    }

    /** Arithmetic CAPTCHA (+, -, ×). Answer is the numeric result string. */
    public static CaptchaDTO generateMath() throws Exception {
        return buildMath(new Random());
    }

    /** Auto: randomly choose TEXT or MATH. */
    public static CaptchaDTO generate(CaptchaDTO.Mode mode) throws Exception {
        Random rng = new Random();
        if (mode == CaptchaDTO.Mode.AUTO) mode = rng.nextBoolean() ? CaptchaDTO.Mode.TEXT : CaptchaDTO.Mode.MATH;
        return (mode == CaptchaDTO.Mode.MATH) ? generateMath() : generateText(5);
    }

    // =========================================================================
    // Math builder
    // =========================================================================

    private static CaptchaDTO buildMath(Random rng) throws Exception {
        int ops = 1 + rng.nextInt(2); // 1 or 2 operators
        int[] nums = new int[ops + 1];
        char[] opArr = new char[ops];

        for (int i = 0; i <= ops; i++) nums[i] = 1 + rng.nextInt(9);
        for (int i = 0; i < ops; i++) opArr[i] = OPS[rng.nextInt(OPS.length)];

        // compute left-to-right (no precedence — keeps it simple)
        int result = nums[0];
        for (int i = 0; i < ops; i++) {
            if (opArr[i] == '+') result += nums[i + 1];
            else if (opArr[i] == '-') result -= nums[i + 1];
            else result *= nums[i + 1];
        }

        StringBuilder sb = new StringBuilder();
        sb.append(nums[0]);
        for (int i = 0; i < ops; i++) sb.append(opArr[i]).append(nums[i + 1]);
        sb.append("=?");

        String display = sb.toString();
        return CaptchaDTO.builder()
            .image(DATA_URI_PREFIX + toBase64(renderText(display)))
            .answer(String.valueOf(result))
            .build();
    }

    // =========================================================================
    // Rendering core
    // =========================================================================

    private static BufferedImage renderText(String text) {
        Random rng = new Random();

        // adapt width to character count
        int charCount = text.length();
        int w = Math.max(IMG_W, charCount * 34 + 24);
        int h = IMG_H;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        hint(g);

        // ── gradient background ───────────────────────────────────────────────
        GradientPaint gp = new GradientPaint(0, 0, new Color(0xFF, 0xFE, 0xF0), w, h, new Color(0xFF, 0xF0, 0xF8));
        g.setPaint(gp);
        g.fillRoundRect(0, 0, w, h, 22, 22);

        // ── polka-dot texture ─────────────────────────────────────────────────
        for (int i = 0; i < 180; i++) {
            int x = rng.nextInt(w);
            int y = rng.nextInt(h);
            Color c = COLORS[rng.nextInt(COLORS.length)];
            int alpha = 15 + rng.nextInt(35);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            int sz = 2 + rng.nextInt(5);
            g.fillOval(x, y, sz, sz);
        }

        // ── draw characters ───────────────────────────────────────────────────
        int slotW = w / (charCount + 1);
        int baseY = h / 2 + 13;

        for (int i = 0; i < charCount; i++) {
            char ch = text.charAt(i);
            boolean isMathSym = (ch == '+' || ch == '-' || ch == '\u00D7' || ch == '=' || ch == '?');

            Color col = COLORS[(i * 3 + rng.nextInt(3)) % COLORS.length];

            int fs = isMathSym ? 26 + rng.nextInt(6) : 28 + rng.nextInt(10);
            Font font = new Font(FONTS[rng.nextInt(FONTS.length)], Font.BOLD, fs);

            // math symbols rotate less so they stay readable
            double maxAngle = isMathSym ? 18.0 : 48.0;
            double angle = Math.toRadians((rng.nextDouble() - 0.5) * maxAngle);

            int x = slotW / 2 + i * slotW + rng.nextInt(8) - 4;
            int y = baseY + rng.nextInt(12) - 6;

            AffineTransform saved = g.getTransform();
            g.rotate(angle, x, y - fs / 2);
            g.setFont(font);

            // white cartoon outline
            drawOutline(g, String.valueOf(ch), x, y, 2, new Color(255, 255, 255, 210));
            // subtle dark inner shadow
            drawOutline(g, String.valueOf(ch), x, y, 1, new Color(0, 0, 0, 35));
            // main colored glyph
            g.setColor(col);
            g.drawString(String.valueOf(ch), x, y);

            g.setTransform(saved);
        }

        // ── bubble decorations ────────────────────────────────────────────────
        // drawBubbles(g, rng, w, h);

        // ── two wavy noise lines ──────────────────────────────────────────────
        drawWaves(g, rng, w, h);

        // ── rounded border ────────────────────────────────────────────────────
        // g.setStroke(new BasicStroke(2.0f));
        // g.setColor(new Color(0xCC, 0xBB, 0x99, 170));
        // g.drawRoundRect(1, 1, w - 3, h - 3, 22, 22);

        g.dispose();
        return img;
    }

    // ── drawing helpers ───────────────────────────────────────────────────────

    private static void drawOutline(Graphics2D g, String s, int x, int y, int r, Color c) {
        g.setColor(c);
        for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) if (dx != 0 || dy != 0) g.drawString(
            s,
            x + dx,
            y + dy
        );
    }

    // private static void drawBubbles(Graphics2D g, Random rng, int w, int h) {
    // int count = 12 + rng.nextInt(8);
    // for (int i = 0; i < count; i++) {
    // int r = 5 + rng.nextInt(12);
    // int x = r + rng.nextInt(w - r * 2);
    // int y = r + rng.nextInt(h - r * 2);
    // Color c = COLORS[rng.nextInt(COLORS.length)];

    // // translucent bubble fill
    // g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
    // g.fillOval(x - r, y - r, r * 2, r * 2);

    // // coloured rim
    // g.setStroke(new BasicStroke(1.6f));
    // g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 175));
    // g.drawOval(x - r, y - r, r * 2, r * 2);

    // // inner shimmer arc
    // g.setColor(new Color(255, 255, 255, 110));
    // g.setStroke(new BasicStroke(1.3f));
    // g.drawArc(x - r + 2, y - r + 2, (int) (r * 0.85), (int) (r * 0.85), 40, 110);

    // // bright shine dot
    // g.setColor(new Color(255, 255, 255, 230));
    // int sd = Math.max(2, r / 3);
    // g.fillOval(x - r / 3, y - r / 3, sd, sd);
    // }
    // }

    private static void drawWaves(Graphics2D g, Random rng, int w, int h) {
        for (int wave = 0; wave < 2; wave++) {
            Color lc = COLORS[rng.nextInt(COLORS.length)];
            g.setColor(new Color(lc.getRed(), lc.getGreen(), lc.getBlue(), 55));
            g.setStroke(new BasicStroke(1.5f));
            double phase = rng.nextDouble() * Math.PI * 2;
            double amp = 9 + rng.nextDouble() * 11;
            double freq = 0.04 + rng.nextDouble() * 0.04;
            int yBase = (wave == 0) ? h / 3 : (2 * h) / 3;
            int px = 0;
            int py = yBase + (int) (amp * Math.sin(phase));
            for (int x = 6; x <= w; x += 6) {
                int y = yBase + (int) (amp * Math.sin(freq * x + phase));
                g.drawLine(px, py, x, y);
                px = x;
                py = y;
            }
        }
    }

    private static void hint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static String randomText(int length) {
        Random rng = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(MIXED.charAt(rng.nextInt(MIXED.length())));
        return sb.toString();
    }

    private static String toBase64(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // =========================================================================
    // Smoke-test (javac ColorfulCaptcha.java && java ColorfulCaptcha)
    // =========================================================================

    public static void main(String[] args) throws Exception {
        System.out.println("=== ColorfulCaptcha smoke-test ===\n");

        // TEXT
        CaptchaDTO text = generateText(5);
        System.out.println("[TEXT]  image length : " + text.getImage().length());
        save(text, "captcha_text.png");

        // MATH
        CaptchaDTO math = generateMath();
        System.out.println("[MATH]  image length : " + math.getImage().length());
        save(math, "captcha_math.png");

        // AUTO x3
        for (int i = 1; i <= 3; i++) {
            CaptchaDTO auto = generate(CaptchaDTO.Mode.AUTO);
            System.out.printf("[AUTO%d] image length : %d%n", i, auto.getImage().length());
            save(auto, "captcha_auto" + i + ".png");
        }
    }

    private static void save(CaptchaDTO r, String filename) throws Exception {
        String base64 = r.getImage().replace(DATA_URI_PREFIX, "");
        byte[] bytes = Base64.getDecoder().decode(base64);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        ImageIO.write(img, "PNG", new java.io.File(filename));
        System.out.println("        saved   : " + filename + "\n");
    }
}
