/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Info Support BV
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cc.args;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class Args
 *
 * Schema description:
 * <table>
 *     <th>
 *         <td>Code</td>
 *         <td>Type</td>
 *     </th>
 *     <tr>
 *         <td><code>#</code></td>
 *         <td>integer</td>
 *     </tr>
 *     <tr>
 *         <td><code>##</code></td>
 *         <td>double</td>
 *     </tr>
 *     <tr>
 *         <td><code>*</code></td>
 *         <td>string</td>
 *     </tr>
 *     <tr>
 *         <td><code>[*]</code></td>
 *         <td>varargs</td>
 *     </tr>
 *     <tr>
 *         <td><code></code> (nothing specified)</td>
 *         <td>boolean</td>
 *     </tr>
 * </table>
 *
 * So a valid schema is: <code>"a,b#,c*"</code>.
 *
 * With can then be retrieved:
 * <code>
 *     Args args = new Args("a,b#,c*", arguments); // arguments = '-a true -b 1 -c "Hello"'
 *     args.getB("a"); // Returns boolean
 *     args.getI("b"); // Returns integer
 *     args.getS("c"); // Returns String
 * </code>
 *
 * TODO:
 * Refactor this code so it is easier to implement the <code>getI()</code> functionality.
 */
public class Args {
    /**
     * The format that needs to be checked.
     */
    private String[] arrayFmt;

    /**
     * The arguments that were given.
     */
    private String[] arrayArgs = new String[0];

    /**
     * The variable in which the booleans get stored
     */
    private Map bools = new HashMap();

    /**
     * The variable in which the strings get stored
     */
    private Map strs = new HashMap();

    /**
     * Construct a new instance of the Args class
     * @param fmt The format to use
     * @param args The arguments to extract
     * @throws ArgsParseException An args parse exception
     */
    public Args(String fmt, String args) throws ArgsParseException {
        this.arrayFmt = Arrays.stream(fmt.split(","))
                .filter(s -> s != null)
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .toArray(String[]::new);
        this.prs(args);

        if (this.arrayArgs.length / 2 != this.arrayFmt.length) {
            throw new ArgsParseException("Number of supplied args did not match number of expected args");
        }

        for (int i = 0; i < arrayFmt.length; i++) {
            String typeIndicator = arrayFmt[i].substring(arrayFmt[i].length() - 1);
            if (typeIndicator.equals("*")) {
                String v = this.arrayArgs[(i * 2) + 1]; // Value from arguments

                String[] exploded = Arrays.stream(v.split("\"")).toArray(String[]::new);
                //String[] exploded = Arrays.stream(v.split("\"")).toArray(String[]::new);

                if (exploded.length != 2) {
                    throw new ArgsParseException("Expected 1 value");
                }

                String val = exploded[1];
                String indexFmt = arrayFmt[i].replaceAll("\\*", "").trim();
                this.strs.put(indexFmt, val);
            } else {
                String v = this.arrayArgs[(i * 2) + 1]; // Value from arguments

                if (!v.equals("true") && !v.equals("false")) {
                    throw new ArgsParseException("Expected either 'true' or 'false' for boolean arg");
                }

                boolean val = v.toLowerCase().equals("true");
                this.bools.put(this.arrayFmt[i], val);
            }
        }
    }

    /**
     * Parse the arguments
     * @param args the arguments
     */
    private void prs(String args) {
        String[] tmp = Arrays.stream(args.split("-"))
                .filter(s -> s != null)
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .toArray(String[]::new);
        for(String arg: tmp) {
            String[] exploded = Arrays.stream(arg.split(" ", 2))
                    .filter(s -> s != null)
                    .map(String::trim)
                    .filter(s -> s.length() > 0)
                    .toArray(String[]::new);

            String[] tmp2 = new String[this.arrayArgs.length + 2];
            System.arraycopy(this.arrayArgs, 0, tmp2, 0, this.arrayArgs.length);
            tmp2[this.arrayArgs.length + 0] = exploded[0];
            tmp2[this.arrayArgs.length + 1] = exploded[1];

            this.arrayArgs = tmp2;
        }
    }

//    private String rmQts(String input) {
//        return Arrays.stream(input.split("\"")).limit(3).toArray(String[]::new)[1]
//    }

    /**
     * Get a boolean
     * @param k The key
     * @return null is no entry found, else the boolean
     */
    public boolean getB(String k) {
        return (boolean) bools.get(k);
    }

    /**
     * Get a String
     * @param k The key
     * @return null is no entry found, else the string
     */
    public String getS(String k) {
        return (String) strs.get(k);
    }
}
