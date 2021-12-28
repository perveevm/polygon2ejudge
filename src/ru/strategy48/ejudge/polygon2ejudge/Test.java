package ru.strategy48.ejudge.polygon2ejudge;

import ru.strategy48.ejudge.polygon2ejudge.contest.compile.JavaCompiler;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;
import ru.strategy48.ejudge.polygon2ejudge.contest.ContestUtils;
import ru.strategy48.ejudge.polygon2ejudge.polygon.objects.Package;
import ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions.PolygonException;
import ru.strategy48.ejudge.polygon2ejudge.polygon.PolygonSession;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        PolygonSession session = new PolygonSession("", "");
        try {
            System.out.println(JavaCompiler.getInstance().findMainClassName(Path.of("/Users/mihailperveev/polygon2ejudge/src/ru/strategy48/ejudge/polygon2ejudge/gen.java")));
//            List<Package> packages = session.getProblemPackages(160888);
//            int someId = packages.get(0).getId();
//
////            ContestUtils.prepareProblem(session, 144627, Paths.get("/Users/mihailperveev/polygon2ejudge/problems/"), "Generic", 1, "A");
////            ContestUtils.prepareProblem(session, 143952, Paths.get("/Users/mihailperveev/polygon2ejudge/problems/"), "Generic", 1, "A");
//
//            ContestUtils.prepareContest(session, 18732, Paths.get("/Users/mihailperveev/polygon2ejudge/problems/"), "Generic", Paths.get("/Users/mihailperveev/polygon2ejudge/default.cfg"));
        } catch (ContestException e) {
            e.printStackTrace();
        }
    }
}
