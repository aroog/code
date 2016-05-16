package ast;

// TODO: HIGH. Rename: NewExpression->ClassInstanceCreation or ObjectCreation
// TODO: HIGH. Rename: FieldAccess or FieldRead. Right now, inconsistent.
public enum ExpressionType { NewExpression, MethodInvocation, FieldRead, FieldWrite, Unknown, ArrayCreation, ArrayRead, ArrayWrite, LoadLiteral};
