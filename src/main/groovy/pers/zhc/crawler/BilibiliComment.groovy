package pers.zhc.crawler

import org.json.JSONArray
import org.json.JSONObject
import pers.zhc.jni.sqlite.SQLite3
import pers.zhc.util.IOUtils
import pers.zhc.util.Random

/**
 * @author bczhc
 */
class BilibiliComment {
    static def oid = 419423169
    static def mode = Mode.TIME_SORT

    static {
        JNILoader.load("/home/bczhc/code/jni/build/libjni-lib.so")
    }

    static void main(String[] args) {
        def db = SQLite3.open("comments.db")
        db.exec("""CREATE TABLE IF NOT EXISTS comment
(
    -- timestamp
    ctime    INTEGER,
    message TEXT NOT NULL,
    -- member id
    mid      INTEGER,
    -- username
    uname    TEXT NOT NULL
)""")
        def statement = db.compileStatement("INSERT INTO comment (ctime, message, mid, uname) VALUES (?, ?, ?, ?)")

        int next = 0
        def page = 0
        def totalCount = 0
        while (true) {
            def infos = fetchInfos(next)
            next = infos[1] as int

            def replies = infos[0] as JSONArray
            def count = replies.size()
            totalCount += count
            def isEnd = infos[2] as boolean

            def i = 0
            db.beginTransaction()
            replies.forEach {
                def timestamp =it["ctime"]
                def member = it["member"]
                def username = member["uname"]
                def mid = member["mid"] as int
                def mid2 = it["mid"]
                assert mid == mid2
                def message = it["content"]["message"]

                statement.reset()
                statement.bind([timestamp, message, mid, username] as Object[])
                statement.step()

                println "page: $page, i: $i"
                ++i
            }
            db.commit()
            println "totalCount: $totalCount"

            if (isEnd) break
            ++page
            Thread.sleep(Random.generate(5000, 6000))
        }

        statement.release()
        db.close()
    }

    static def fetchInfos(int next) {
        def json = fetchDataJSON(next)
        def cursor = json["data"]["cursor"]
        def replies = json["data"]["replies"] as JSONArray
        def isEnd = cursor["is_end"]
        def nextNext = cursor["next"] as int

        return [replies, nextNext, isEnd]
    }

    static def fetchDataJSON(int next) {
        def url = getURL(next)
        def is = url.openStream()
        def read = readToString(is)
        is.close()

        return new JSONObject(read)
    }

    static URL getURL(int next) {
        return getURL(oid, mode, next)
    }

    static URL getURL(int oid, Mode mode, int next) {
        return new URL(String.format("https://api.bilibili.com/x/v2/reply/main?oid=%d&type=1&mode=%d&next=%d&plat=1", oid, mode.mode, next))
    }

    enum Mode {
        TIME_SORT(2),
        HOT_SORT(3)

        def mode

        Mode(int mode) {
            this.mode = mode
        }
    }

    static String readToString(InputStream is) {
        def out = new ByteArrayOutputStream()

        def reader = is.newReader("UTF-8")
        def br = new BufferedReader(reader)
        IOUtils.streamWrite(is, out)
        br.close()
        reader.close()

        return out.toString()
    }
}
