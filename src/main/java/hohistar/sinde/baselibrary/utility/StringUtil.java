package hohistar.sinde.baselibrary.utility;

import java.text.DecimalFormat;

/**
 *
 */
public class StringUtil {

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    /**
     * 把 223344.0 格式化为 223,344.00 整数位超过6位未处理
     */
    public static String fmtMicrometer(double number) {

        String text = String.valueOf(number);
        DecimalFormat df;
        if (text.indexOf(".") > 0) {
            if (text.length() - text.indexOf(".") == 1) {
                df = new DecimalFormat("###,##0.");
            } else if (text.length() - text.indexOf(".") == 2) {
                df = new DecimalFormat("###,##0.0");
            } else {
                df = new DecimalFormat("###,##0.00");
            }
        } else {
            df = new DecimalFormat("###,##0");
        }

        return df.format(number);
    }

    public static String fmtMicrometer(String text) {

        if (text == null || text.trim().equals("")) {
            return null;
        }

        try {
            double number = Double.parseDouble(text);
            return fmtMicrometer(number);
        } catch (NumberFormatException e) {

            return null;
        }
    }
}
