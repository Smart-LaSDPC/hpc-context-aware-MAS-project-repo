package br.icmc.lasdpc.utils;

import org.apache.jena.reasoner.rulesys.BuiltinRegistry;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;

public class CustomFunctions2 {

    private static OntModel model;

    public static void setModel(OntModel model) {
        CustomFunctions2.model = model;
    }

    public static class GreaterThan extends BaseBuiltin {
        @Override
        public String getName() {
            return "greaterThan";
        }

        @Override
        public boolean bodyCall(Node[] args, int length, RuleContext context) {
            Node n1 = getArg(0, args, context);
            Node n2 = getArg(1, args, context);

            if (n1.isLiteral() && n2.isLiteral()) {
                int value1 = Integer.valueOf(n1.getLiteralValue().toString());
                int value2 = Integer.valueOf(n2.getLiteralValue().toString());
                return value1 > value2;
            }
            return false;
        }
    }

    public static class LessThanOrEqual extends BaseBuiltin {
        @Override
        public String getName() {
            return "lessThanOrEqual";
        }

        @Override
        public boolean bodyCall(Node[] args, int length, RuleContext context) {
            Node n1 = getArg(0, args, context);
            Node n2 = getArg(1, args, context);

            if (n1.isLiteral() && n2.isLiteral()) {
                int value1 = Integer.valueOf(n1.getLiteralValue().toString());
                int value2 = Integer.valueOf(n2.getLiteralValue().toString());
                return value1 <= value2;
            }
            return false;
        }
    }

    public static class ChangeStateProperty extends BaseBuiltin {
        @Override
        public String getName() {
            return "changeStateProperty";
        }

        @Override
        public void headAction(Node[] args, int length, RuleContext context) {
            Node n1 = getArg(0, args, context);
            Node n2 = getArg(1, args, context);
            Node n3 = getArg(2, args, context);

            if (model != null) {
                Resource subject = model.getResource(n1.getURI());
                Property predicate = model.getProperty(n2.getURI());
                Resource object = model.getResource(n3.getURI());

                model.removeAll(subject, predicate, null);
                model.add(subject, predicate, object);
            }
        }

        @Override
        public boolean bodyCall(Node[] args, int length, RuleContext context) {
            return true; // This function does not need to be used in the body
        }
    }
    
    public static class ChangeStatePropertyInteger extends BaseBuiltin {
        @Override
        public String getName() {
            return "changeStatePropertyInteger";
        }

        @Override
        public void headAction(Node[] args, int length, RuleContext context) {
        	//System.out.println(":::::::::::::::::::::::::::::::> Into in ChangeStatePropertyInteger")        	
            Node n1 = getArg(0, args, context);// Id of Lamp :> http://www.ex.com/demox/lamp6
            Node n2 = getArg(1, args, context);// Data property :> http://www.ex.com/demox/has_state
            Node n3 = getArg(2, args, context);// Value :> -5
                        
            if (model != null) {                              	
            	Resource subject = model.getResource(n1.getURI());
                Property predicate = model.getProperty(n2.getURI());
                int intValue = Integer.parseInt(n3.getLiteralValue().toString());
                Literal literalValue = model.createTypedLiteral(intValue, XSDDatatype.XSDinteger);                
                //System.out.println("subject>>>>>>> " + subject);
                //System.out.println("predicate>>>>> " + predicate);
                //System.out.println("literalValue>>>>> " + literalValue);                
                model.removeAll(subject, predicate, null);
                model.add(subject, predicate, literalValue);                
            }
        }

        @Override
        public boolean bodyCall(Node[] args, int length, RuleContext context) {
        	
            return true; // This function does not need to be used in the body
        }
    }
    
    
    public static class ChangeStatePropertyString extends BaseBuiltin {
        
        @Override
        public String getName() {
            return "changeStatePropertyString";
        }

        @Override
        public void headAction(Node[] args, int length, RuleContext context) {
            //System.out.println(":::::::::::::::::::::::::::::::> Into ChangeStatePropertyString");

            // Get the arguments: subject, predicate, and the string value
            Node n1 = getArg(0, args, context); // Resource: the subject, e.g., http://www.ex.com/demox/room1
            Node n2 = getArg(1, args, context); // Property: the predicate, e.g., http://www.ex.com/demox/has_occupancy_status
            Node n3 = getArg(2, args, context); // String value: e.g., "unoccupied"
            
            if (model != null) {
                // Get the subject as a resource and the predicate as a property
                Resource subject = model.getResource(n1.getURI());
                Property predicate = model.getProperty(n2.getURI());
                String stringValue = n3.getLiteralValue().toString(); // Convert to string
                
                // Create a string literal for the value
                Literal literalValue = model.createLiteral(stringValue);
                
                // Remove the old value and add the new string value to the model
                model.removeAll(subject, predicate, null);
                model.add(subject, predicate, literalValue);
            }
        }

        @Override
        public boolean bodyCall(Node[] args, int length, RuleContext context) {
            return true; // No need for body logic, just return true
        }
    }


    public static void registerCustomFunctions() {
        BuiltinRegistry.theRegistry.register(new GreaterThan());
        BuiltinRegistry.theRegistry.register(new LessThanOrEqual());
        BuiltinRegistry.theRegistry.register(new ChangeStateProperty());
        BuiltinRegistry.theRegistry.register(new ChangeStatePropertyInteger());
        BuiltinRegistry.theRegistry.register(new ChangeStatePropertyString());
    }
}
