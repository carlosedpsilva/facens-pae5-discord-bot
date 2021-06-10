package com.lojfacens.pitchy.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class made by AdrianTodt (and modified by Kodehawa and vaaaarlos) with a lot of useful and fast {@link String} and String[] utilities methods.
 */
public class StringUtils {

  public static final Pattern SPLIT_PATTERN = Pattern.compile("\\s+");
  public static final String[] EMPTY_ARRAY = new String[0];

  private StringUtils() { }

  /**
   * Splits a {@link String} into an Array considering quoted args as one
   * argument, unescapes each arg characters with {@link StringUtils#unescape(String)}
   * and returns an Array normalized by {@link StringUtils#normalizeArray(String[], int)}
   *
   * @param args         the {@link String} to be splitted
   * @param expectedArgs the final size of the Array
   * @return             {@link String}[] with the size of expectedArgs
   */
  public static String[] advancedSplitArgs(String args, int expectedArgs) {
    var result = new ArrayList<String>();

    var inBlock = false;
    var currentBlock = new StringBuilder();

    for (var i = 0; i < args.length(); i++) {
      var currentChar = args.charAt(i);

      // Flip inBlock if current character is a " or a start/end smart quote character (“ or ”) (but only if it's not escaped)
      if ((currentChar == '"' || currentChar == '“' || currentChar == '”')
          && (i == 0 || args.charAt(i - 1) != '\\' || args.charAt(i - 2) == '\\'))
        inBlock = !inBlock;

      // If character is currently in a block, append to the current block (will append space characters)
      if (inBlock) currentBlock.append(currentChar);
      // If character is not currently in a block and is a space, add argument to result and reset block
      else if (Character.isSpaceChar(currentChar)) {
        // Ignore excessive space characters between arguments
        if (Character.isSpaceChar(args.charAt(i + 1))) continue;
        if (currentBlock.length() != 0) {
          // Check if first or last character is a " or a start/end smart quote character (“ or ”) and remove them
          if ((currentBlock.charAt(0) == '"' || currentBlock.charAt(0) == '“')
              && (currentBlock.charAt(currentBlock.length() - 1) == '"' || currentBlock.charAt(currentBlock.length() - 1) == '”')) {
            // Remove start and end quote
            currentBlock.deleteCharAt(0);
            currentBlock.deleteCharAt(currentBlock.length() - 1);
          }
          // Add the unboxed result to the current block and reset the current block
          result.add(unescape(currentBlock.toString()));
          currentBlock = new StringBuilder();
        } else {
          // Append current char to block
          currentBlock.append(currentChar);
        }
      }
      // If character is not currently in a block and is a space, append to current block
      else currentBlock.append(currentChar);
    }

    if (currentBlock.length() != 0) {
      // Check if first or last character is a " or a start/end smart quote character (“ or ”) and remove them
      if ((currentBlock.charAt(0) == '"' || currentBlock.charAt(0) == '“')
          && (currentBlock.charAt(currentBlock.length() - 1) == '"' || currentBlock.charAt(currentBlock.length() - 1) == '”')) {
        // Remove start and end quote
        currentBlock.deleteCharAt(0);
        currentBlock.deleteCharAt(currentBlock.length() - 1);
      }
      // Remove escape characters
      result.add(unescape(currentBlock.toString()));
    }

    var raw = result.toArray(EMPTY_ARRAY);
    if (expectedArgs < 1) return raw;
    return normalizeArray(raw, expectedArgs);
  }

  /**
   * Limits a {@link String} to the specified length
   *
   * @param value  the {@link String} to be limited
   * @param length the final size of the {@link String}
   * @return       {@link String} with the size of length
   */
  public static String limit(String value, int length) {
    var buf = new StringBuilder(value);
    if (buf.length() > length && length >= 3) {
      buf.setLength(length - 3);
      buf.append("...");
      return buf.toString();
    }
    return new String(new char[length > 0 ? length : 0]).replace("\0", ".");
  }

  /**
   * Normalize a {@link String} array, returning it with the size of expectedArgs
   * + 1, where the last element is the remaining arguments of the raw
   * {@link String}[]
   *
   * @param raw          the {@link String}[] array to be normalized
   * @param expectedArgs the final size of the Array
   * @return             @{link String}[] with the size of expectedArgs + 1
   */
  public static String[] normalizeArray(String[] raw, int expectedArgs) {
    var normalized = new String[expectedArgs + 1];
    Arrays.fill(normalized, "");
    for (var i = 0; i < raw.length; i++) {
      if (raw[i] != null && !raw[i].isEmpty()) {
        if (i + 1 < normalized.length)
          normalized[i] = raw[i];
        else {
          var remaining = Arrays.copyOfRange(raw, i, raw.length);
          normalized[i] = Arrays.stream(remaining).collect(Collectors.joining(" "));
          break;
        }
      }
    }
    return normalized;
  }

  /**
   * Parses arguments to a {@link Map} with Strings which starts with {@code -},
   * {@code --} or {@code /} as keys and the following arguments which does not as
   * parameters in a {@link List}
   *
   * @param args the {@link String}[] to be parsed
   * @return     a {@link Map} with options as keys and parameters as values
   */
  public static Map<String, List<String>> parseArguments(String[] args) {
    var options = new HashMap<String, List<String>>();
    var optArgs = new ArrayList<String>();
    var currentOption = "default";
    for (var i = 0; i < args.length; i++) {
      if (args[i].charAt(0) == '-' || args[i].charAt(0) == '/') {     // Check whether this arg is an option
        args[i] = args[i].substring(1);                               // Remove first dash or slash
        if (args[i].charAt(0) == '-') args[i] = args[i].substring(1); // Remove second dash
        currentOption = args[i];                                      // Update current option
      } else optArgs.add(args[i]);                                    // Add current arg to list if it isnt an option
      // Add following arguments to list while next arg isnt an option
      while (i + 1 < args.length && args[i + 1].charAt(0) != '-' && args[i + 1].charAt(0) != '/') {
        i++;
        optArgs.add(args[i]);
      }
      options.put(currentOption, optArgs);
      optArgs = new ArrayList<>();
    }
    return options;
  }

  /**
   * Enhanced {@link String#split(String, int)} with {@code \\s+} as the
   * {@link Pattern} used, which returns an Array normalized by
   * {@link StringUtils#normalizeArray(String[], int)}.
   *
   * @param args         the {@link String} to be split
   * @param expectedArgs the size of the returned array of Non-null {@link String}s
   * @return             a {@link String}[] with the size of expectedArgs + 1
   */
  public static String[] splitArgs(String args, int expectedArgs) {
    var raw = SPLIT_PATTERN.split(args, expectedArgs);
    if (expectedArgs < 1) return raw;
    return normalizeArray(raw, expectedArgs);
  }

  /**
   * Unescapes {@code \n}, {@code \r}, {@code \t}, {@code "} and {@code \} from
   * this {@link String}
   *
   * @param str the {@link String} to be unescaped
   */
  public static String unescape(String str) {
    return str.replace("\\n", "\n")
              .replace("\\r", "\r")
              .replace("\\t", "\t")
              .replace("\\\"", "\"")
              .replace("\\\\", "\\");
  }

}
