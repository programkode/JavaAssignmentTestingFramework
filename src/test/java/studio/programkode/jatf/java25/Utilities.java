package studio.programkode.jatf.java25;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;


public class Utilities
{
    static public String dedent(String input) {
        var output = new ArrayList<String>();
        var indentation = 0;
        var found = false;

        Iterator<String> iterator = input.lines().iterator();

        // for every line
        while (iterator.hasNext()) {
            var line = iterator.next();

            IO.println("Before: %s".formatted(line));

            // with no indentation
            if (line.stripLeading().equals(line)) {
                // simply pass-through
                output.add(line);

                IO.println("After*: %s".formatted(line));
            }
            // or when indentation exists
            else {
                // figure out length of it
                var currentIndentation = Utilities.stringIndentationLength(line);

                // and when no indentation level is set, use first found
                if (!found) {
                    indentation = currentIndentation;
                    found = true;
                }

                // use the lesser of indentation level found and current line indentation level
                // and store line as-is from indentation level used


                var substring = line.substring(Math.min(indentation, currentIndentation));

                output.add(substring);

                IO.println("After: %s".formatted(substring));
            }
        }

        return String.join("\n", output);
    }


    static public int stringIndentationLength(String input) {
        return input.length() - input.stripLeading().length();
    }


    static public int streamStringGetSmallestIndentationLength(Stream<String> lines) {
        int output = 8;
        int indentation = 0;
        var iterator = lines.iterator();

        while (iterator.hasNext()) {
            var line = iterator.next();

            indentation = line.length() - line.stripLeading().length();

            if (indentation > 0 && indentation < output) {
                output = indentation;
            }
        }

        return output;
    }

    static public String numberZeroPad(int input) {
        return numberZeroPad(input, 8);
    }

    static public String numberZeroPad(int input, int precision) {
        var inputLength = Integer.toString(input).length();
        var diff = precision - inputLength;

        return "%s%s".formatted("0".repeat(Math.max(0, diff)), input);

    }
}
