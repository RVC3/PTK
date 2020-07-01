package ru.ppr.core.manager.emito;

/**
 * Класс для формирования имитовставки
 *
 * @author Sergey Kolesnikov
 */

public class EmitoSecurity {

    //Вспомогательный блок (накопитель)
    private long[] N = {0, 0, 0, 0, 0, 0, 0, 0};

    //Ключ
    private long[] KEY =
            {
                    1550025511,
                    669395643,
                    1665628773,
                    1346184539,
                    884619151,
                    1187993639,
                    146265,
                    1719527700
            };

    //Таблица замен
    private int[][] TABLE = {
            {2, 5, 6, 15, 1, 8, 12, 4, 7, 10, 14, 0, 9, 3, 11, 13},
            {10, 5, 1, 2, 9, 3, 12, 8, 13, 4, 0, 14, 11, 15, 7, 6},
            {5, 2, 3, 0, 13, 8, 1, 12, 14, 4, 6, 15, 11, 7, 9, 10},
            {8, 7, 3, 11, 2, 6, 14, 5, 10, 9, 15, 4, 13, 12, 0, 1},
            {10, 4, 13, 14, 6, 9, 11, 0, 2, 8, 15, 7, 3, 12, 5, 1},
            {4, 6, 7, 15, 9, 8, 0, 13, 12, 14, 5, 10, 2, 1, 3, 11},
            {8, 11, 12, 9, 10, 14, 7, 1, 0, 2, 13, 3, 15, 5, 6, 4},
            {1, 9, 15, 4, 8, 14, 6, 7, 10, 2, 13, 12, 5, 3, 0, 11}
    };

    // Первый блок открытых данных
    private int[] T1 = new int[64];

    // Второй блок открытых данных
    private int[] T2 = new int[64];

    //Правый блок
    private int[] A = new int[32];

    //Левый блок
    private int[] B = new int[32];

    //Временный блок
    private int[] C = new int[32];

    //Блок с подключем
    private int[] K = new int[32];

    //Блок с результатом суммирования по модулю 2^32
    private int[] SUM32 = new int[32];

    //Блок с результатом прогона через таблицу замены
    private int[] N1 = new int[32];

    //Блок с результатом по битового суммирования по модулю 2
    private int[] F = new int[32];


    // Запуск шифрования
    private char[] Start(char[] data) {
        insertT(data);            //Заполнение блоков
        Procces(T1);            // Первый прогон
        getXORT1T2();            // Суммирование результата со вторым блоком
        Procces(T2);            // Второй прогон

        return getResToStr();            // Возвращаем A[] переведенный в string

    }

    // Основной процесс шифрования
    private void Procces(int[] T) {
        insertA(T);                  //Заполнение блока A
        insertB(T);                  //Заполнение блока B

        for (int t = 1; t <= 16; t++)     //Начало основного цикла:
        {
            toInsertK(t);            //Заполнение подключа K
            //Функция Фейстеля:
            toSum232();               //1) Сумматор по модулю 2^32
            insertN();              //2) Заполнение накопителя
            replaceNode();          //3) Проход через узлы замен
            insertN1();             //4) Заполнение выходного накопителя
            shift11();              //5) Сдвиг на 11 битов влево
            //Конец функции Фейстеля.
            XOR();                  //Двоичное исключающее "или" для Bi и F. Результат сохраняем в At+1
        }
        Result(T);                  //Склеивание блоков A33+B33
    }

    //Заполнение блоков
    private void insertT(char[] data) {
        //Заполнение блоков символами
        char ch;
        long n;

        //Блок Т2
        for (int i = 0; i < 4; i++) {
            ch = data[8 + i];              //Берем символ
            n = ch;                       //Сохраняем его номер
            for (int j = 0; j < 8; j++)        //Заполняем блок битами символов (число символов N<8)
            {
                if (n / (long) getPow(2, 7 - j) >= 1) {
                    T2[8 * i + j] = 1;
                    n = n - (long) getPow(2, 7 - j);
                } else
                    T2[8 * i + j] = 0;
            }
        }

        for (int i = 32; i < 64; i++)     //Заполняем оставшуюся часть блока нулями.
            T2[i] = 0;

        // Блок Т1
        for (int i = 0; i < 8; i++) {
            ch = data[i];                 //Берем символ
            n = ch;                       //Сохраняем его номер
            for (int j = 0; j < 8; j++)        //Заполняем блок битами символов (число символов N=8)
            {
                if (n / (long) getPow(2, 7 - j) >= 1) {
                    T1[8 * i + j] = 1;
                    n = n - (long) getPow(2, 7 - j);
                } else
                    T1[8 * i + j] = 0;
            }
        }
    }

    //Возведение в степень
    private long getPow(int a, int b) {
        long rez = 1;
        for (int k = 1; k <= b; k++) {
            rez = rez * (long) a;
        }
        return rez;
    }

    //Заполнение блока B
    private void insertB(int[] T) {   //Заполнение блока В
        for (int i = 0; i < 32; i++)
            B[i] = T[i];
    }

    //Заполнение блока A
    private void insertA(int[] T) {   //Заполнение блока A
        for (int i = 0; i < 32; i++)
            A[i] = T[32 + i];
    }

    //Заполнение блока K
    private void toInsertK(int t) {
        //Заполнение и вывод подключа K
        long x = 0;
        x = KEY[(t - 1) % 8];

        //Переводим значение подключа в массив битов
        for (int i = 0; i < 32; i++) {
            if (x / getPow(2, 31 - i) >= 1) {
                K[i] = 1;
                x = x - getPow(2, 31 - i);
            } else
                K[i] = 0;
        }
    }

    //Сумматор по модулю Pow(2,32)=2^32
    private void toSum232() {
        for (int c = 0, i = 31; i >= 0; i--)      //Поразрядное сложение блоков
        {
            if ((A[i] + K[i] + c) >= 2)       //Если переполнение, то:
            {
                SUM32[i] = A[i] + K[i] + c - 2;
                c = 1;
            } else {
                SUM32[i] = A[i] + K[i] + c;
                c = 0;
            }
        }
    }

    //Заполнение накопителя
    private void insertN() {
        for (int i = 0; i < 8; i++)            //В накопителе 8 чисел
        {
            for (int j = 0; j < 4; j++)        //Преобразуем 4 бита сумматора (начиная с конца, т.е. 4 посл бита, 4 предпосл бита и т.д.) в 10-чное число
                if (SUM32[31 - 4 * i - j] == 1)
                    N[i] = (long) ((long) N[i] + getPow(2, j));
        }
    }

    //Прохождение накопителя через узлы замен
    private void replaceNode() {   //Прохождение накопителя через узлы замен
        for (long i = 0, k; i < 8; i++) {
            k = N[(int) i];
            N[(int) i] = TABLE[(int) i][(int) k];           //Выход из i-ого узла
        }
    }

    //Преобразование результатов функции ReplaceNode() в накопитель N1
    private void insertN1() {
        //Преобразование результатов функции ReplaceNode() в накопитель N1
        for (int i = 0; i < 8; i++)            //Все 8 чисел накопителя N
            for (int j = 0; j < 4; j++)        //переводим в двоичный вид
            {
                if (N[i] / (int) getPow(2, 3 - j) >= 1) {
                    N1[4 * i + j] = 1;
                    N[i] = (long) ((long) N[i] - getPow(2, 3 - j));
                } else
                    N1[4 * i + j] = 0;
            }
    }

    //Сдвиг на 11 бит влево
    private void shift11() {
        //Сдвиг на 11 бит влево
        //Сохраняем биты с 12 по 32
        for (int i = 0; i < 21; i++)
            F[i] = N1[i + 11];

        //Сохраняем биты с 1 по 11
        for (int i = 0; i < 11; i++)
            F[i + 21] = N1[i];
    }

    //По байтовое суммирование результата функции и блока В
    private void XOR() {
        //Сохраняем блок A во временное хранилище
        for (int i = 0; i < 32; i++)
            C[i] = A[i];

        //Двоичное исключающее "ИЛИ"
        for (int i = 0; i < 32; i++)
            A[i] = F[i] ^ B[i];

        // Из временного хранилища в блок В
        for (int i = 0; i < 32; i++)
            B[i] = C[i];
    }

    //По байтовое суммирование блока 1 и блока2
    private void getXORT1T2() {
        for (int i = 0; i < 64; i++)
            T2[i] = T1[i] ^ T2[i];    //Двоичное исключающее "ИЛИ"
    }

    //Склеивание блоков. T=AB
    private void Result(int[] T) {   //Склеивание блоков. T=AB

        for (int i = 0; i < 32; i++)
            T[i] = A[i];

        for (int i = 32; i < 64; i++)
            T[i] = B[i - 32];
    }

    //Перевод массива бит в массив char
    private char[] getResToStr() {
        char[] res = new char[4];
        for (int j = 0; j < 4; j++) {
            int sum = 0;
            for (int i = 0; i < 8; i++) {
                sum += A[j * 8 + i] * (int) getPow(2, 7 - i);
            }

            res[j] = (char) sum;
        }
        return res;
    }

    public int emitoCreate(byte[] data) {

        char[] str = new char[data.length];
        char temp;
        int itemp = 0;
        int result = 0;

        for (int i = 0; i < 12; i++) {
            str[i] = (char) (data[i] & 0xFF);
        }

        char[] emito = getEmito89(str);

        temp = emito[0];
        itemp |= temp;
        itemp <<= 24;
        result |= itemp;
        itemp = 0;

        temp = emito[1];
        itemp |= temp;
        itemp <<= 16;
        result |= itemp;
        itemp = 0;

        temp = emito[2];
        itemp |= temp;
        itemp <<= 8;
        result |= itemp;
        itemp = 0;

        temp = emito[3];
        itemp |= temp;
        result |= itemp;

        return result;
    }

    private char[] getEmito89(char[] data) {
        return this.Start(data);
    }

}