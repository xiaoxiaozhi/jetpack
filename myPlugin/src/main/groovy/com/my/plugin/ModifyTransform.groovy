import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project
import org.apache.commons.io.FileUtils

public class ModifyTransform extends Transform {
    def project
//    内存   windown   1  android 2
    def pool = ClassPool.default
//查找类
    ModifyTransform(Project project) {
        this.project = project
    }
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        project.android.bootClasspath.each {
            pool.appendClassPath(it.absolutePath)
        }
//       1 拿到输入
        transformInvocation.inputs.each {
//            class 1     ---> 文件夹     jar 可能 1  不可能2  N
            it.directoryInputs.each {

                def preFileName =    it.file.absolutePath
                pool.insertClassPath(preFileName)

                println "========directoryInputs======== " + preFileName
                findTarget(it.file, preFileName)
//  it.file
                //       2 查询输出的文件夹    目的地
                def dest =   transformInvocation.outputProvider.getContentLocation(
                        it.name,
                        it.contentTypes,
                        it.scopes,
                        Format.DIRECTORY
                )

                //       3  文件copy  ---》 下一个环节
                FileUtils.copyDirectory(it.file, dest)
            }
            it.jarInputs.each {
                def dest = transformInvocation.outputProvider.getContentLocation(it.name
                        , it.contentTypes, it.scopes, Format.JAR)
//                    去哪里
                FileUtils.copyFile(it.file, dest)
            }
//            修改class   不是修改 jar
        }

//       2 查询输出的文件夹    目的地

//       3  文件copy  ---》 下一个环节
//        想干嘛干嘛
    }

    @Override
    String getName() {
        return "david"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }
}