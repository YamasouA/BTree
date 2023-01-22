import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BTree {
    private static final int M = 2;
    private int insertKey;
    private Page insertPage;
    private boolean undersize;

    private class Page {
        int n = 0; // データ数
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

        // i: 何番目に値を入れるか
        private void insertItem(int i, int newKey, Page newPage) {
            // 右に値を動かす
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

        private boolean insertNode() {
            int i = 0;
            while (i < n && key[i] < insertKey)
                i++;
            if (i < n && key[i] == insertKey) {
                message = "もう登録されています";
                return true;
            }
            // 子ノードに登録されているもしくは登録できた
            if (branch[i] != null && branch[i].insertNode())
                return true;
            // キーが存在しない && データが満杯じゃない
            if (n < 2 * M) {
                insertItem(i, insertKey, insertPage);
                return true;
            } else {
                split(i);
                return false;
            }
        }

        // key[i], branch[i + 1]を削除する。ページが小さくなり過ぎるとundersizeフラグを立てる
        private void deleteItem(int i) {
            while (++i < n) {
                key[i - 1] = key[i];
                branch[i] = branch[i + 1];
            }
            branch[n] = null;
            undersize = (--n < M);
        }

        // branch[i - 1]の一番右をkey[i - 1]を経由してbranch[i]に動かす
        private void moveRight(int i) {
            Page left = branch[i - 1];
            Page right = branch[i];
            right.insertItem(0, key[i - 1], right.branch[i]);
            // right.insertItem(0, left.key[left.n - 1], left.branch[left.n]);
            // これでよくね？
            // insertItemで仮置きしたkeyをleftの一番右に書き換える
            key[i - 1] = left.key[left.n - 1];
            // right.branch[i]で仮置きした値をleft.branchの一番右で置き換える
            right.branch[0] = left.branch[left.n];
            left.n--;
        }

        private void moveLeft(int i) {
            Page left = branch[i - 1];
            Page right = branch[i];
            left.insertItem(left.n, key[i - 1], right.branch[0]);
            key[i - 1] = right.key[0];
            // deleteItemで消えね？
            right.branch[0] = right.branch[1];
            right.deleteItem(0);
        }

        // branch[i - 1]とbranch[i]を結合する
        private void combine(int i) {
            Page left = branch[i - 1];
            Page right = branch[i];

            for (int j = 1; j <= right.n; j++)
                left.insertItem(left.n,
                    right.key[j - 1], right.branch[j]);
            deleteItem(i - 1);
        }

        // 小さくなりすぎたページbranch[i]を修復する
        private void restore(int i) {
            undersize = false;

            // データは一つづつ入れるはずだから一つ移動させればOK
            if (i > 0) {
                if (branch[i - 1].n > M)
                    moveRight(i);
                else
                    combine(i);
            } else {
                if (branch[1].n > M)
                    moveLeft(1);
                else
                    combine(1);
            }
        }

        private boolean deleteNode(int deleteKey) {
            int i = 0;
            boolean deleted = false;
            while (i < n && key[i] < deleteKey)
                i++;
            if (i < n && key[i] == deleteKey) { // 見つかった
                deleted = true;
                // 削除は必ず葉で行う
                // deletekeyが葉ではないときは葉の持つdeletekeyの次の値と入れ替えて削除する
                Page q = branch[i + 1];
                // qが葉かノードかの確認
                if (q != null) {
                    // 葉まで潜る
                    while (q.branch[0] != null)
                        q = q.branch[0];
                    // 葉のkeyを削除するキーに代入してdeleteKeyを葉のキーに変更
                    key[i] = deleteKey = q.key[0];
                    // 再帰的に呼び出す
                    branch[i + 1].deleteNode(deleteKey);
                    if (undersize)
                        restore(i + 1);
                } else
                    deleteItem(i);
            } else {
                if (branch[i] != null) {
                    deleted = branch[i].deleteNode(deleteKey);
                    if (undersize)
                        restore(i);
                }
            }
            return deleted;
        }

        private void print() {
            System.out.print("(");
            for (int i = 0; i <= n; i++) {
                if (branch[i] == null)
                    System.out.print(".");
                else
                    branch[i].print();
                if (i < n)
                    System.out.print(key[i]);
            }
            System.out.print(")");
        }
    }
    private Page root = new Page();
    static String message = "";

    public void searchNode(int key) {
        if (root.searchNode(key))
            message = "見つかりました";
        else
            message = "見つかりませんでした";
    }

    public void insertNode(int key) {
        message = "登録しました";
        insertKey = key;
        insertPage = null;
        if (root != null && root.insertNode())
            return;
        // splitがrootまで返って来た場合
        Page p = new Page();
        p.branch[0] = root;
        root = p;
        p.insertItem(0, insertKey, insertPage);
    }

    public void deleteNode(int key) {
        undersize = false;
        if (root.deleteNode(key)) { // 根から再帰的に木を辿って削除する
            if (root.n == 0)
                root = root.branch[0];
            message = "削除しました";
        } else
            message = "見つかりません";
    }

    public void print() {
        if (root != null)
            root.print();
        else
            System.out.print(".");
        System.out.println();
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
