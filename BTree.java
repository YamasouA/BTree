import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BTree {
    private static final int M = 2;
    private int insertKey;
    private Page insertPage;
    private boolean undersize;

    private class Page {
        int n = 0;
        int[] key = new int[2 * M];
        Page[] branch = new Page[2 * M + 1];

        private boolean searchNode(int searchKey) {
            int i = 0;
            while (i < n && key[i] < searchKey)
                i++;
            if (i < n && key[i] == searchKey)
                return true;
            if (branch[i] == null)
                return false;
            return branch[i].searchNode(searchKey);
        }

        private void insertItem(int i, int newKey, Page newPage) {
            // keyを見るついでに右に動かす
            for (int j = n; j > i; j--) {
                branch[j + 1] = branch[j];
                key[j] = key[j - 1];
            }
            key[i] = newKey;
            branch[i + 1] = newPage;
            n++;
        }

        private void split(int i) {
            final int m;
            if (i < M)
                m = M;
            else
                m = M + 1;
            Page q = new Page();
            for (int j = m + 1; j <= 2 * M; j++) {
                q.key[j - m - 1] = key[j - 1];
                q.branch[j - m] = branch[j];
            }
            q.n = 2 * M - m;
            n = m;
            if (i <= M)
                insertItem(i, insertKey, insertPage);
            else
                q.insertItem(i - m, insertKey, insertPage);
            insertKey = key[n - 1];
            q.branch[0] = branch[n];
            n--;
            insertPage = q;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        BTree tree = new BTree();

        for (;;) {
            System.out.print("挿入 In, 検索 Sn, 削除 Dn (n: 整数)?");
            String s = input.readLine();
            if (s == null)
                break;
            int key = Integer.parseInt(s.substring(1));
            switch (Character.toUpperCase(s.charAt(0))) {
                case 'I':
                    tree.insertNode(key);
                    break;
                case 'S':
                    tree.searchNode(key);
                    break;
                case 'D':
                    tree.deleteNode(key);
                    break;
                default:
                    message = "????";
                    break;
            }
            System.out.println(message);
            tree.print();


            }
        }
    }
}
