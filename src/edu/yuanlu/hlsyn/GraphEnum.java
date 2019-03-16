package edu.yuanlu.hlsyn;

public class GraphEnum {

		public enum VertexType{
			IF, WHILE, INOP, ONOP, ADD, SUB, MUL, DIV, MOD, MUX, SHL, SHR, REG, COMPGT, COMPLT, COMPEQ; //14 kinds in total
		}
		
		public enum VertexColor{
			BLACK, GREY, WHITE;
		}
		
		public enum EdgeSign{
			SIGN, UNSIGN;
		}
		
//		public enum EdgeWidth{
//			SIGNED_1, SIGNED_8, SIGNED_16, SIGNED_32, SIGNED_64, UNSIGNED_1, UNSIGNED_8, UNSIGNED_16, UNSIGNED_32, UNSIGNED_64;
//		}
		
		public enum ConstructionMode{
			EMPTY, NOP, ERROR;
		}
		
		public enum EdgeType{
			INPUT, OUTPUT, VARIABLE;
		}
		
		public enum BlockType{
			IF, ELSE, WHILE, MAIN;
		}
		

}
