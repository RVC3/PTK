package ru.ppr.ikkm;

import java.util.HashMap;

/**
 * Created by Александр on 20.01.2016.
 * <p>
 * Name:     cp866_DOSCyrillicRussian to Unicode tabl
 * Unicode version: 2.0
 * Table version: 2.00
 * Table format:  Format A
 * Date:          04/24/96
 * Authors:       Lori Brownell <loribr@microsoft.com>
 * K.D. Chang    <a-kchang@microsoft.com>
 * General notes: none
 * <p>
 * Format: Three tab-separated columns
 * Column #1 is the cp866_DOSCyrillicRussian code (in hex)
 * Column #2 is the Unicode (in hex as 0xXXXX)
 * Column #3 is the Unicode name (follows a comment sign, '#')
 * <p>
 * The entries are in cp866_DOSCyrillicRussian order
 */
public class CP866 {

    static final HashMap<Character, Byte> CHAR_MAP = new HashMap<>();

    static final char UNKNOWN_CHAR = '?';

    static {
        CHAR_MAP.put('\u0000', (byte) 0x00); // NULL
        CHAR_MAP.put('\u0001', (byte) 0x01); // START OF    HEADING
        CHAR_MAP.put('\u0002', (byte) 0x02); // START OF    TEXT
        CHAR_MAP.put('\u0003', (byte) 0x03); // END OF    TEXT
        CHAR_MAP.put('\u0004', (byte) 0x04); // END OF    TRANSMISSION
        CHAR_MAP.put('\u0005', (byte) 0x05); // ENQUIRY
        CHAR_MAP.put('\u0006', (byte) 0x06); // ACKNOWLEDGE
        CHAR_MAP.put('\u0007', (byte) 0x07); // BELL
        CHAR_MAP.put('\u0008', (byte) 0x08); // BACKSPACE
        CHAR_MAP.put('\u0009', (byte) 0x09); // HORIZONTAL TABULATION
        //CHAR_MAP.put('u000a', (byte) 0x0a); // LINE FEED
        CHAR_MAP.put('\n', (byte) 0x0a); // LINE FEED
        CHAR_MAP.put('\u000b', (byte) 0x0b); // VERTICAL TABULATION
        CHAR_MAP.put('\u000c', (byte) 0x0c); // FORM FEED
        //CHAR_MAP.put('u000d', (byte) 0x0d); // CARRIAGE RETURN
        CHAR_MAP.put('\r', (byte) 0x0d); // CARRIAGE RETURN
        CHAR_MAP.put('\u000e', (byte) 0x0e); // SHIFT OUT
        CHAR_MAP.put('\u000f', (byte) 0x0f); // SHIFT IN
        CHAR_MAP.put('\u0010', (byte) 0x10); // DATA LINK    ESCAPE
        CHAR_MAP.put('\u0011', (byte) 0x11); // DEVICE CONTROL    ONE
        CHAR_MAP.put('\u0012', (byte) 0x12); // DEVICE CONTROL    TWO
        CHAR_MAP.put('\u0013', (byte) 0x13); // DEVICE CONTROL    THREE
        CHAR_MAP.put('\u0014', (byte) 0x14); // DEVICE CONTROL    FOUR
        CHAR_MAP.put('\u0015', (byte) 0x15); // NEGATIVE ACKNOWLEDGE
        CHAR_MAP.put('\u0016', (byte) 0x16); // SYNCHRONOUS IDLE
        CHAR_MAP.put('\u0017', (byte) 0x17); // END OF    TRANSMISSION BLOCK
        CHAR_MAP.put('\u0018', (byte) 0x18); // CANCEL
        CHAR_MAP.put('\u0019', (byte) 0x19); // END OF    MEDIUM
        CHAR_MAP.put('\u001a', (byte) 0x1a); // SUBSTITUTE
        CHAR_MAP.put('\u001b', (byte) 0x1b); // ESCAPE
        CHAR_MAP.put('\u001c', (byte) 0x1c); // FILE SEPARATOR
        CHAR_MAP.put('\u001d', (byte) 0x1d); // GROUP SEPARATOR
        CHAR_MAP.put('\u001e', (byte) 0x1e); // RECORD SEPARATOR
        CHAR_MAP.put('\u001f', (byte) 0x1f); // UNIT SEPARATOR
        CHAR_MAP.put('\u0020', (byte) 0x20); // SPACE
        CHAR_MAP.put('\u0021', (byte) 0x21); // EXCLAMATION MARK
        CHAR_MAP.put('\u0022', (byte) 0x22); // QUOTATION MARK
        CHAR_MAP.put('\u0023', (byte) 0x23); // NUMBER SIGN
        CHAR_MAP.put('\u0024', (byte) 0x24); // DOLLAR SIGN
        CHAR_MAP.put('\u0025', (byte) 0x25); // PERCENT SIGN
        CHAR_MAP.put('\u0026', (byte) 0x26); // AMPERSAND
        //CHAR_MAP.put('u0027', (byte) 0x27); // APOSTROPHE
        CHAR_MAP.put('\'', (byte) 0x27); // APOSTROPHE
        CHAR_MAP.put('\u0028', (byte) 0x28); // LEFT PARENTHESIS
        CHAR_MAP.put('\u0029', (byte) 0x29); // RIGHT PARENTHESIS
        CHAR_MAP.put('\u002a', (byte) 0x2a); // ASTERISK
        CHAR_MAP.put('\u002b', (byte) 0x2b); // PLUS SIGN
        CHAR_MAP.put('\u002c', (byte) 0x2c); // COMMA
        CHAR_MAP.put('\u002d', (byte) 0x2d); // HYPHEN-MINUS
        CHAR_MAP.put('\u002e', (byte) 0x2e); // FULL STOP
        CHAR_MAP.put('\u002f', (byte) 0x2f); // SOLIDUS
        CHAR_MAP.put('\u0030', (byte) 0x30); // DIGIT ZERO
        CHAR_MAP.put('\u0031', (byte) 0x31); // DIGIT ONE
        CHAR_MAP.put('\u0032', (byte) 0x32); // DIGIT TWO
        CHAR_MAP.put('\u0033', (byte) 0x33); // DIGIT THREE
        CHAR_MAP.put('\u0034', (byte) 0x34); // DIGIT FOUR
        CHAR_MAP.put('\u0035', (byte) 0x35); // DIGIT FIVE
        CHAR_MAP.put('\u0036', (byte) 0x36); // DIGIT SIX
        CHAR_MAP.put('\u0037', (byte) 0x37); // DIGIT SEVEN
        CHAR_MAP.put('\u0038', (byte) 0x38); // DIGIT EIGHT
        CHAR_MAP.put('\u0039', (byte) 0x39); // DIGIT NINE
        CHAR_MAP.put('\u003a', (byte) 0x3a); // COLON
        CHAR_MAP.put('\u003b', (byte) 0x3b); // SEMICOLON
        CHAR_MAP.put('\u003c', (byte) 0x3c); // LESS-    THAN SIGN
        CHAR_MAP.put('\u003d', (byte) 0x3d); // EQUALS SIGN
        CHAR_MAP.put('\u003e', (byte) 0x3e); // GREATER-    THAN SIGN
        CHAR_MAP.put('\u003f', (byte) 0x3f); // QUESTION MARK
        CHAR_MAP.put('\u0040', (byte) 0x40); // COMMERCIAL AT
        CHAR_MAP.put('\u0041', (byte) 0x41); // LATIN CAPITAL    LETTER A
        CHAR_MAP.put('\u0042', (byte) 0x42); // LATIN CAPITAL    LETTER B
        CHAR_MAP.put('\u0043', (byte) 0x43); // LATIN CAPITAL    LETTER C
        CHAR_MAP.put('\u0044', (byte) 0x44); // LATIN CAPITAL    LETTER D
        CHAR_MAP.put('\u0045', (byte) 0x45); // LATIN CAPITAL    LETTER E
        CHAR_MAP.put('\u0046', (byte) 0x46); // LATIN CAPITAL    LETTER F
        CHAR_MAP.put('\u0047', (byte) 0x47); // LATIN CAPITAL    LETTER G
        CHAR_MAP.put('\u0048', (byte) 0x48); // LATIN CAPITAL    LETTER H
        CHAR_MAP.put('\u0049', (byte) 0x49); // LATIN CAPITAL    LETTER I
        CHAR_MAP.put('\u004a', (byte) 0x4a); // LATIN CAPITAL    LETTER J
        CHAR_MAP.put('\u004b', (byte) 0x4b); // LATIN CAPITAL    LETTER K
        CHAR_MAP.put('\u004c', (byte) 0x4c); // LATIN CAPITAL    LETTER L
        CHAR_MAP.put('\u004d', (byte) 0x4d); // LATIN CAPITAL    LETTER M
        CHAR_MAP.put('\u004e', (byte) 0x4e); // LATIN CAPITAL    LETTER N
        CHAR_MAP.put('\u004f', (byte) 0x4f); // LATIN CAPITAL    LETTER O
        CHAR_MAP.put('\u0050', (byte) 0x50); // LATIN CAPITAL    LETTER P
        CHAR_MAP.put('\u0051', (byte) 0x51); // LATIN CAPITAL    LETTER Q
        CHAR_MAP.put('\u0052', (byte) 0x52); // LATIN CAPITAL    LETTER R
        CHAR_MAP.put('\u0053', (byte) 0x53); // LATIN CAPITAL    LETTER S
        CHAR_MAP.put('\u0054', (byte) 0x54); // LATIN CAPITAL    LETTER T
        CHAR_MAP.put('\u0055', (byte) 0x55); // LATIN CAPITAL    LETTER U
        CHAR_MAP.put('\u0056', (byte) 0x56); // LATIN CAPITAL    LETTER V
        CHAR_MAP.put('\u0057', (byte) 0x57); // LATIN CAPITAL    LETTER W
        CHAR_MAP.put('\u0058', (byte) 0x58); // LATIN CAPITAL    LETTER X
        CHAR_MAP.put('\u0059', (byte) 0x59); // LATIN CAPITAL    LETTER Y
        CHAR_MAP.put('\u005a', (byte) 0x5a); // LATIN CAPITAL    LETTER Z
        CHAR_MAP.put('\u005b', (byte) 0x5b); // LEFT SQUARE    BRACKET
        //CHAR_MAP.put('u005c', (byte) 0x5c); // REVERSE SOLIDUS
        CHAR_MAP.put('\\', (byte) 0x5c); // REVERSE SOLIDUS
        CHAR_MAP.put('\u005d', (byte) 0x5d); // RIGHT SQUARE    BRACKET
        CHAR_MAP.put('\u005e', (byte) 0x5e); // CIRCUMFLEX ACCENT
        CHAR_MAP.put('\u005f', (byte) 0x5f); // LOW LINE
        CHAR_MAP.put('\u0060', (byte) 0x60); // GRAVE ACCENT
        CHAR_MAP.put('\u0061', (byte) 0x61); // LATIN SMALL    LETTER A
        CHAR_MAP.put('\u0062', (byte) 0x62); // LATIN SMALL    LETTER B
        CHAR_MAP.put('\u0063', (byte) 0x63); // LATIN SMALL    LETTER C
        CHAR_MAP.put('\u0064', (byte) 0x64); // LATIN SMALL    LETTER D
        CHAR_MAP.put('\u0065', (byte) 0x65); // LATIN SMALL    LETTER E
        CHAR_MAP.put('\u0066', (byte) 0x66); // LATIN SMALL    LETTER F
        CHAR_MAP.put('\u0067', (byte) 0x67); // LATIN SMALL    LETTER G
        CHAR_MAP.put('\u0068', (byte) 0x68); // LATIN SMALL    LETTER H
        CHAR_MAP.put('\u0069', (byte) 0x69); // LATIN SMALL    LETTER I
        CHAR_MAP.put('\u006a', (byte) 0x6a); // LATIN SMALL    LETTER J
        CHAR_MAP.put('\u006b', (byte) 0x6b); // LATIN SMALL    LETTER K
        CHAR_MAP.put('\u006c', (byte) 0x6c); // LATIN SMALL    LETTER L
        CHAR_MAP.put('\u006d', (byte) 0x6d); // LATIN SMALL    LETTER M
        CHAR_MAP.put('\u006e', (byte) 0x6e); // LATIN SMALL    LETTER N
        CHAR_MAP.put('\u006f', (byte) 0x6f); // LATIN SMALL    LETTER O
        CHAR_MAP.put('\u0070', (byte) 0x70); // LATIN SMALL    LETTER P
        CHAR_MAP.put('\u0071', (byte) 0x71); // LATIN SMALL    LETTER Q
        CHAR_MAP.put('\u0072', (byte) 0x72); // LATIN SMALL    LETTER R
        CHAR_MAP.put('\u0073', (byte) 0x73); // LATIN SMALL    LETTER S
        CHAR_MAP.put('\u0074', (byte) 0x74); // LATIN SMALL    LETTER T
        CHAR_MAP.put('\u0075', (byte) 0x75); // LATIN SMALL    LETTER U
        CHAR_MAP.put('\u0076', (byte) 0x76); // LATIN SMALL    LETTER V
        CHAR_MAP.put('\u0077', (byte) 0x77); // LATIN SMALL    LETTER W
        CHAR_MAP.put('\u0078', (byte) 0x78); // LATIN SMALL    LETTER X
        CHAR_MAP.put('\u0079', (byte) 0x79); // LATIN SMALL    LETTER Y
        CHAR_MAP.put('\u007a', (byte) 0x7a); // LATIN SMALL    LETTER Z
        CHAR_MAP.put('\u007b', (byte) 0x7b); // LEFT CURLY    BRACKET
        CHAR_MAP.put('\u007c', (byte) 0x7c); // VERTICAL LINE
        CHAR_MAP.put('\u007d', (byte) 0x7d); // RIGHT CURLY    BRACKET
        CHAR_MAP.put('\u007e', (byte) 0x7e); // TILDE
        CHAR_MAP.put('\u007f', (byte) 0x7f); // DELETE
        CHAR_MAP.put('\u0410', (byte) 0x80); // CYRILLIC CAPITAL    LETTER A
        CHAR_MAP.put('\u0411', (byte) 0x81); // CYRILLIC CAPITAL    LETTER BE
        CHAR_MAP.put('\u0412', (byte) 0x82); // CYRILLIC CAPITAL    LETTER VE
        CHAR_MAP.put('\u0413', (byte) 0x83); // CYRILLIC CAPITAL    LETTER GHE
        CHAR_MAP.put('\u0414', (byte) 0x84); // CYRILLIC CAPITAL    LETTER DE
        CHAR_MAP.put('\u0415', (byte) 0x85); // CYRILLIC CAPITAL    LETTER IE
        CHAR_MAP.put('\u0416', (byte) 0x86); // CYRILLIC CAPITAL    LETTER ZHE
        CHAR_MAP.put('\u0417', (byte) 0x87); // CYRILLIC CAPITAL    LETTER ZE
        CHAR_MAP.put('\u0418', (byte) 0x88); // CYRILLIC CAPITAL    LETTER I
        CHAR_MAP.put('\u0419', (byte) 0x89); // CYRILLIC CAPITAL    LETTER SHORT    I
        CHAR_MAP.put('\u041a', (byte) 0x8a); // CYRILLIC CAPITAL    LETTER KA
        CHAR_MAP.put('\u041b', (byte) 0x8b); // CYRILLIC CAPITAL    LETTER EL
        CHAR_MAP.put('\u041c', (byte) 0x8c); // CYRILLIC CAPITAL    LETTER EM
        CHAR_MAP.put('\u041d', (byte) 0x8d); // CYRILLIC CAPITAL    LETTER EN
        CHAR_MAP.put('\u041e', (byte) 0x8e); // CYRILLIC CAPITAL    LETTER O
        CHAR_MAP.put('\u041f', (byte) 0x8f); // CYRILLIC CAPITAL    LETTER PE
        CHAR_MAP.put('\u0420', (byte) 0x90); // CYRILLIC CAPITAL    LETTER ER
        CHAR_MAP.put('\u0421', (byte) 0x91); // CYRILLIC CAPITAL    LETTER ES
        CHAR_MAP.put('\u0422', (byte) 0x92); // CYRILLIC CAPITAL    LETTER TE
        CHAR_MAP.put('\u0423', (byte) 0x93); // CYRILLIC CAPITAL    LETTER U
        CHAR_MAP.put('\u0424', (byte) 0x94); // CYRILLIC CAPITAL    LETTER EF
        CHAR_MAP.put('\u0425', (byte) 0x95); // CYRILLIC CAPITAL    LETTER HA
        CHAR_MAP.put('\u0426', (byte) 0x96); // CYRILLIC CAPITAL    LETTER TSE
        CHAR_MAP.put('\u0427', (byte) 0x97); // CYRILLIC CAPITAL    LETTER CHE
        CHAR_MAP.put('\u0428', (byte) 0x98); // CYRILLIC CAPITAL    LETTER SHA
        CHAR_MAP.put('\u0429', (byte) 0x99); // CYRILLIC CAPITAL    LETTER SHCHA
        CHAR_MAP.put('\u042a', (byte) 0x9a); // CYRILLIC CAPITAL    LETTER HARD    SIGN
        CHAR_MAP.put('\u042b', (byte) 0x9b); // CYRILLIC CAPITAL    LETTER YERU
        CHAR_MAP.put('\u042c', (byte) 0x9c); // CYRILLIC CAPITAL    LETTER SOFT    SIGN
        CHAR_MAP.put('\u042d', (byte) 0x9d); // CYRILLIC CAPITAL    LETTER E
        CHAR_MAP.put('\u042e', (byte) 0x9e); // CYRILLIC CAPITAL    LETTER YU
        CHAR_MAP.put('\u042f', (byte) 0x9f); // CYRILLIC CAPITAL    LETTER YA
        CHAR_MAP.put('\u0430', (byte) 0xa0); // CYRILLIC SMALL    LETTER A
        CHAR_MAP.put('\u0431', (byte) 0xa1); // CYRILLIC SMALL    LETTER BE
        CHAR_MAP.put('\u0432', (byte) 0xa2); // CYRILLIC SMALL    LETTER VE
        CHAR_MAP.put('\u0433', (byte) 0xa3); // CYRILLIC SMALL    LETTER GHE
        CHAR_MAP.put('\u0434', (byte) 0xa4); // CYRILLIC SMALL    LETTER DE
        CHAR_MAP.put('\u0435', (byte) 0xa5); // CYRILLIC SMALL    LETTER IE
        CHAR_MAP.put('\u0436', (byte) 0xa6); // CYRILLIC SMALL    LETTER ZHE
        CHAR_MAP.put('\u0437', (byte) 0xa7); // CYRILLIC SMALL    LETTER ZE
        CHAR_MAP.put('\u0438', (byte) 0xa8); // CYRILLIC SMALL    LETTER I
        CHAR_MAP.put('\u0439', (byte) 0xa9); // CYRILLIC SMALL    LETTER SHORT    I
        CHAR_MAP.put('\u043a', (byte) 0xaa); // CYRILLIC SMALL    LETTER KA
        CHAR_MAP.put('\u043b', (byte) 0xab); // CYRILLIC SMALL    LETTER EL
        CHAR_MAP.put('\u043c', (byte) 0xac); // CYRILLIC SMALL    LETTER EM
        CHAR_MAP.put('\u043d', (byte) 0xad); // CYRILLIC SMALL    LETTER EN
        CHAR_MAP.put('\u043e', (byte) 0xae); // CYRILLIC SMALL    LETTER O
        CHAR_MAP.put('\u043f', (byte) 0xaf); // CYRILLIC SMALL    LETTER PE
        CHAR_MAP.put('\u2591', (byte) 0xb0); // LIGHT SHADE
        CHAR_MAP.put('\u2592', (byte) 0xb1); // MEDIUM SHADE
        CHAR_MAP.put('\u2593', (byte) 0xb2); // DARK SHADE
        CHAR_MAP.put('\u2502', (byte) 0xb3); // BOX DRAWINGS    LIGHT VERTICAL
        CHAR_MAP.put('\u2524', (byte) 0xb4); // BOX DRAWINGS    LIGHT VERTICAL    AND LEFT
        CHAR_MAP.put('\u2561', (byte) 0xb5); // BOX DRAWINGS    VERTICAL SINGLE    AND LEFT    DOUBLE
        CHAR_MAP.put('\u2562', (byte) 0xb6); // BOX DRAWINGS    VERTICAL DOUBLE    AND LEFT    SINGLE
        CHAR_MAP.put('\u2556', (byte) 0xb7); // BOX DRAWINGS    DOWN DOUBLE    AND LEFT    SINGLE
        CHAR_MAP.put('\u2555', (byte) 0xb8); // BOX DRAWINGS    DOWN SINGLE    AND LEFT    DOUBLE
        CHAR_MAP.put('\u2563', (byte) 0xb9); // BOX DRAWINGS    DOUBLE VERTICAL    AND LEFT
        CHAR_MAP.put('\u2551', (byte) 0xba); // BOX DRAWINGS    DOUBLE VERTICAL
        CHAR_MAP.put('\u2557', (byte) 0xbb); // BOX DRAWINGS    DOUBLE DOWN    AND LEFT
        CHAR_MAP.put('\u255d', (byte) 0xbc); // BOX DRAWINGS    DOUBLE UP    AND LEFT
        CHAR_MAP.put('\u255c', (byte) 0xbd); // BOX DRAWINGS    UP DOUBLE    AND LEFT    SINGLE
        CHAR_MAP.put('\u255b', (byte) 0xbe); // BOX DRAWINGS    UP SINGLE    AND LEFT    DOUBLE
        CHAR_MAP.put('\u2510', (byte) 0xbf); // BOX DRAWINGS    LIGHT DOWN    AND LEFT
        CHAR_MAP.put('\u2514', (byte) 0xc0); // BOX DRAWINGS    LIGHT UP    AND RIGHT
        CHAR_MAP.put('\u2534', (byte) 0xc1); // BOX DRAWINGS    LIGHT UP    AND HORIZONTAL
        CHAR_MAP.put('\u252c', (byte) 0xc2); // BOX DRAWINGS    LIGHT DOWN    AND HORIZONTAL
        CHAR_MAP.put('\u251c', (byte) 0xc3); // BOX DRAWINGS    LIGHT VERTICAL    AND RIGHT
        CHAR_MAP.put('\u2500', (byte) 0xc4); // BOX DRAWINGS    LIGHT HORIZONTAL
        CHAR_MAP.put('\u253c', (byte) 0xc5); // BOX DRAWINGS    LIGHT VERTICAL    AND HORIZONTAL
        CHAR_MAP.put('\u255e', (byte) 0xc6); // BOX DRAWINGS    VERTICAL SINGLE    AND RIGHT    DOUBLE
        CHAR_MAP.put('\u255f', (byte) 0xc7); // BOX DRAWINGS    VERTICAL DOUBLE    AND RIGHT    SINGLE
        CHAR_MAP.put('\u255a', (byte) 0xc8); // BOX DRAWINGS    DOUBLE UP    AND RIGHT
        CHAR_MAP.put('\u2554', (byte) 0xc9); // BOX DRAWINGS    DOUBLE DOWN    AND RIGHT
        CHAR_MAP.put('\u2569', (byte) 0xca); // BOX DRAWINGS    DOUBLE UP    AND HORIZONTAL
        CHAR_MAP.put('\u2566', (byte) 0xcb); // BOX DRAWINGS    DOUBLE DOWN    AND HORIZONTAL
        CHAR_MAP.put('\u2560', (byte) 0xcc); // BOX DRAWINGS    DOUBLE VERTICAL    AND RIGHT
        CHAR_MAP.put('\u2550', (byte) 0xcd); // BOX DRAWINGS    DOUBLE HORIZONTAL
        CHAR_MAP.put('\u256c', (byte) 0xce); // BOX DRAWINGS    DOUBLE VERTICAL    AND HORIZONTAL
        CHAR_MAP.put('\u2567', (byte) 0xcf); // BOX DRAWINGS    UP SINGLE    AND HORIZONTAL    DOUBLE
        CHAR_MAP.put('\u2568', (byte) 0xd0); // BOX DRAWINGS    UP DOUBLE    AND HORIZONTAL    SINGLE
        CHAR_MAP.put('\u2564', (byte) 0xd1); // BOX DRAWINGS    DOWN SINGLE    AND HORIZONTAL    DOUBLE
        CHAR_MAP.put('\u2565', (byte) 0xd2); // BOX DRAWINGS    DOWN DOUBLE    AND HORIZONTAL    SINGLE
        CHAR_MAP.put('\u2559', (byte) 0xd3); // BOX DRAWINGS    UP DOUBLE    AND RIGHT    SINGLE
        CHAR_MAP.put('\u2558', (byte) 0xd4); // BOX DRAWINGS    UP SINGLE    AND RIGHT    DOUBLE
        CHAR_MAP.put('\u2552', (byte) 0xd5); // BOX DRAWINGS    DOWN SINGLE    AND RIGHT    DOUBLE
        CHAR_MAP.put('\u2553', (byte) 0xd6); // BOX DRAWINGS    DOWN DOUBLE    AND RIGHT    SINGLE
        CHAR_MAP.put('\u256b', (byte) 0xd7); // BOX DRAWINGS    VERTICAL DOUBLE    AND HORIZONTAL    SINGLE
        CHAR_MAP.put('\u256a', (byte) 0xd8); // BOX DRAWINGS    VERTICAL SINGLE    AND HORIZONTAL    DOUBLE
        CHAR_MAP.put('\u2518', (byte) 0xd9); // BOX DRAWINGS    LIGHT UP    AND LEFT
        CHAR_MAP.put('\u250c', (byte) 0xda); // BOX DRAWINGS    LIGHT DOWN    AND RIGHT
        CHAR_MAP.put('\u2588', (byte) 0xdb); // FULL BLOCK
        CHAR_MAP.put('\u2584', (byte) 0xdc); // LOWER HALF    BLOCK
        CHAR_MAP.put('\u258c', (byte) 0xdd); // LEFT HALF    BLOCK
        CHAR_MAP.put('\u2590', (byte) 0xde); // RIGHT HALF    BLOCK
        CHAR_MAP.put('\u2580', (byte) 0xdf); // UPPER HALF    BLOCK
        CHAR_MAP.put('\u0440', (byte) 0xe0); // CYRILLIC SMALL    LETTER ER
        CHAR_MAP.put('\u0441', (byte) 0xe1); // CYRILLIC SMALL    LETTER ES
        CHAR_MAP.put('\u0442', (byte) 0xe2); // CYRILLIC SMALL    LETTER TE
        CHAR_MAP.put('\u0443', (byte) 0xe3); // CYRILLIC SMALL    LETTER U
        CHAR_MAP.put('\u0444', (byte) 0xe4); // CYRILLIC SMALL    LETTER EF
        CHAR_MAP.put('\u0445', (byte) 0xe5); // CYRILLIC SMALL    LETTER HA
        CHAR_MAP.put('\u0446', (byte) 0xe6); // CYRILLIC SMALL    LETTER TSE
        CHAR_MAP.put('\u0447', (byte) 0xe7); // CYRILLIC SMALL    LETTER CHE
        CHAR_MAP.put('\u0448', (byte) 0xe8); // CYRILLIC SMALL    LETTER SHA
        CHAR_MAP.put('\u0449', (byte) 0xe9); // CYRILLIC SMALL    LETTER SHCHA
        CHAR_MAP.put('\u044a', (byte) 0xea); // CYRILLIC SMALL    LETTER HARD    SIGN
        CHAR_MAP.put('\u044b', (byte) 0xeb); // CYRILLIC SMALL    LETTER YERU
        CHAR_MAP.put('\u044c', (byte) 0xec); // CYRILLIC SMALL    LETTER SOFT    SIGN
        CHAR_MAP.put('\u044d', (byte) 0xed); // CYRILLIC SMALL    LETTER E
        CHAR_MAP.put('\u044e', (byte) 0xee); // CYRILLIC SMALL    LETTER YU
        CHAR_MAP.put('\u044f', (byte) 0xef); // CYRILLIC SMALL    LETTER YA
        CHAR_MAP.put('\u0401', (byte) 0xf0); // CYRILLIC CAPITAL    LETTER IO
        CHAR_MAP.put('\u0451', (byte) 0xf1); // CYRILLIC SMALL    LETTER IO
        CHAR_MAP.put('\u0404', (byte) 0xf2); // CYRILLIC CAPITAL    LETTER UKRAINIAN    IE
        CHAR_MAP.put('\u0454', (byte) 0xf3); // CYRILLIC SMALL    LETTER UKRAINIAN    IE
        CHAR_MAP.put('\u0407', (byte) 0xf4); // CYRILLIC CAPITAL    LETTER YI
        CHAR_MAP.put('\u0457', (byte) 0xf5); // CYRILLIC SMALL    LETTER YI
        CHAR_MAP.put('\u040e', (byte) 0xf6); // CYRILLIC CAPITAL    LETTER SHORT    U
        CHAR_MAP.put('\u045e', (byte) 0xf7); // CYRILLIC SMALL    LETTER SHORT    U
        CHAR_MAP.put('\u00b0', (byte) 0xf8); // DEGREE SIGN
        CHAR_MAP.put('\u2219', (byte) 0xf9); // BULLET OPERATOR
        CHAR_MAP.put('\u00b7', (byte) 0xfa); // MIDDLE DOT
        CHAR_MAP.put('\u221a', (byte) 0xfb); // SQUARE ROOT
        CHAR_MAP.put('\u2116', (byte) 0xfc); // NUMERO SIGN
        CHAR_MAP.put('\u00a4', (byte) 0xfd); // CURRENCY SIGN
        CHAR_MAP.put('\u25a0', (byte) 0xfe); // BLACK SQUARE
        CHAR_MAP.put('\u00a0', (byte) 0xff); // NO-    BREAK SPACE}
    }

    public static byte[] toBytes(String str) {
        if (str == null) {
            return new byte[0];
        }
        byte[] bytes = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) {
            Byte byteValue = CHAR_MAP.get(str.charAt(i));
            bytes[i] = byteValue == null ? CHAR_MAP.get(UNKNOWN_CHAR) : byteValue;
        }
        return bytes;
    }
}
