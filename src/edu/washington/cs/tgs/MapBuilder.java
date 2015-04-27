package edu.washington.cs.tgs;

import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.ExpandVetoException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MapBuilder {

	private File source;

	public MapBuilder(File source) {
		this.source = source;
	}
	
	public void buildMap () {
		CompilationUnit cu = null;
		try {
			cu = JavaParser.parse(this.source);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ClassVisitor v = new ClassVisitor();
		cu.accept(v, null);

	}
	
	public static class ClassVisitor extends VoidVisitorAdapter<Object> {
		
		String pkg = "";
		LinkedList<String>  cnames = new LinkedList<String>();
		LinkedList<Integer> anonNumbers = new LinkedList<Integer>();
		
		@Override
		public void visit(com.github.javaparser.ast.PackageDeclaration n, Object arg) {
			pkg = n.getName().toStringWithoutComments();
		};
		
		
		@Override
		public void visit(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration typeDecl, Object arg1) {
				anonNumbers.push(0);
				this.pushClass(typeDecl.getName());
				for (BodyDeclaration child : typeDecl.getMembers()) {
					child.accept(this, arg1);
				}
		};		
		
		@Override
		public void visit(com.github.javaparser.ast.body.InitializerDeclaration n, Object arg) {
			handleMethod(n.isStatic() ? "<clinit>" : "<init>", n.getBlock());
			n.accept(new MethodVisiter(), this);			
		};
		
		@Override
		public void visit(MethodDeclaration n, Object arg) {
			handleMethod(n.getName(), n.getBody());
			n.accept(new MethodVisiter(), this);
		}

		///
		public void pushClass(String name) {
			if (cnames.isEmpty()) {
				cnames.push(this.pkg + "." + name);
			} else {
				cnames.push(getCurrentClassName() + "$" + name);
			}
			
			this.anonNumbers.push(0);
		}
		
		public void pushAnon() {
			int num = this.anonNumbers.pop();
			++num;
			this.anonNumbers.push(num);
			cnames.push(getCurrentClassName() + "$" + num);
			this.anonNumbers.push(0);
		}
		
		public void pop() {
			anonNumbers.pop();
			cnames.pop();
		}
		
		public String getCurrentClassName() {
			return cnames.getFirst();
		}
		
		public void handleMethod(MethodDeclaration n) {
		}

		private void handleMethod(String name, BlockStmt body) {
			System.out.println(getCurrentClassName() + "::" + name);			
		}
	}
	
	public static class MethodVisiter extends VoidVisitorAdapter<ClassVisitor> {
		@Override
		public void visit(ObjectCreationExpr n, ClassVisitor arg) {
			if (n.getAnonymousClassBody() != null) {
				arg.pushAnon();
				for (final BodyDeclaration member : n.getAnonymousClassBody()) {
					member.accept(arg, null);
				}
				arg.pop();
			}
		}
	}
	
	
	public static void printAll(Node n, int lvl) {
		for (int i = 0; i < lvl; ++i){
			System.out.print(" ");
		}
		System.out.println(n.getClass());
		for (Node child : n.getChildrenNodes()) {
			printAll(child, lvl + 1);
			System.out.println(child);
		}
	}
	
	
}
