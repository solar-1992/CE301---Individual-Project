package Project.NLP;

import Project.Game.AI.SPL.Orders.AttackOrder;
import Project.Game.AI.SPL.Orders.DefendOrder;
import Project.Game.AI.SPL.Orders.SPLObject;
import Project.Game.Entities.Entity;
import Project.Game.Main;
import Project.Game.Registries.NameRegistry;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Pipeline implements NLPConverter {

    NameRegistry baseRegistry;

    MaxentTagger tagger;

    public Pipeline() {
        baseRegistry = Main.REGISTRY;
        try {
            tagger = new MaxentTagger("Content/NLPModels/english-bidirectional-distsim.tagger");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SPLObject process(String message) {
        String[] temp = extractSenderAndMessage(message);
        // This is needed when we integrate with the game
        String address = temp[0];

        String strippedMessage = temp[1];
        // Store the tagged message
        ArrayList<TaggedWord> taggedMessage = tagMessage(strippedMessage);

        SPLObject splObject = extractOrder(taggedMessage);

        if (splObject != null) splObject.setAddress(address);
        return splObject;
    }

    public String[] extractSenderAndMessage(String message) {
        String[] data = new String[2];

        String regexForStripping = "[A-Za-z]+:\\s*";
        String regexForSender = "[A-Za-z]+:";

        Pattern pattern = Pattern.compile(regexForStripping);
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            data[1] = message.substring(matcher.end());
        }

        pattern = Pattern.compile(regexForSender);
        matcher = pattern.matcher(message);
        while (matcher.find()) {
            data[0] = message.substring(matcher.start(), matcher.end() - 1);
        }

        return data;
    }

    public ArrayList<TaggedWord> tagMessage(String message) {
        System.out.println("Tagging: " + message);

        List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(message));
        ArrayList<TaggedWord> words = tagger.tagSentence(sentences.get(0));
        for (TaggedWord word : words) {
            // Override so that Attack is always VB
            if (word.value().equalsIgnoreCase("Attack")) word.setTag("VB");
            System.out.println(word);
        }
        return words;
    }

    public SPLObject extractOrder(ArrayList<TaggedWord> words) {
        SPLObject object;

        for (TaggedWord word : words) System.out.print(word + " ");
        System.out.println("");

        object = getSimpleAdjectiveOrder(words);
        if (object != null) return object;

        object = getSimpleOrderWithUnits(words);
        if (object != null) return object;

        object = getSimpleOrder(words);
        if (object != null) return object;

        return null;
    }

    private SPLObject getSimpleAdjectiveOrder(ArrayList<TaggedWord> words) {
        List<TaggedWord> match = getFirstInstance(words, "JJ", "VB", "IN", "NN");
        if (match != null) {
            // Adjective gives us strength
            String adjective = match.get(0).value();
            int numberOfUnits = 10;
            switch (adjective.toLowerCase()) {
                case "massive":
                case "intense":
                    numberOfUnits = 45;
                    break;
                case "heavy":
                case "considerable":
                case "huge":
                case "mighty":
                    numberOfUnits = 30;
                    break;
                case "strong":
                case "big":
                case "tough":
                case "substantial":
                case "severe":
                    numberOfUnits = 20;
                    break;
                case "medium":
                case "moderate":
                case "standard":
                    numberOfUnits = 15;
                    break;
                case "small":
                case "little":
                case "midget":
                    numberOfUnits = 10;
                    break;
                case "tiny":
                case "mini":
                    numberOfUnits = 5;
                    break;
            }
            Entity target = baseRegistry.get(match.get(3).value());
            if (target != null) {
                switch (match.get(1).value()) {
                    case "attack":
                    case "offense":
                        return new AttackOrder(target.getLocation(), 10, true, numberOfUnits);
                    case "defense":
                    case "protection":
                        return new DefendOrder(target.getLocation(), 200, numberOfUnits, 10, true);
                }
            }
        }
        return null;
    }

    private SPLObject getSimpleOrderWithUnits(ArrayList<TaggedWord> words) {
        // Attack BS-2 with 10 units
        List<TaggedWord> match = getFirstInstance(words, "VB", "NN", "IN", "CD", "NNS");
        if (match != null) {
            if (baseRegistry.has(match.get(1).value())) {
                Entity target = baseRegistry.get(match.get(1).value());
                switch (match.get(0).value().toLowerCase()) {
                    case "attack":
                    case "destroy":
                    case "kill":
                        return new AttackOrder(target.getLocation(), 10, true, Integer.parseInt(match.get(3).value()));
                    case "defend":
                    case "protect":
                        return new DefendOrder(target.getLocation(), 200, Integer.parseInt(match.get(3).value()), 10, true);
                }
            }
        }
        return null;
    }

    private SPLObject getSimpleOrder(ArrayList<TaggedWord> words) {
        // Hunt for simple order - not sure this one will come up much
        // Attack BV-23
        List<TaggedWord> match = getFirstInstance(words, "VB", "NN");
        if (match != null) {
            switch (match.get(0).value().toLowerCase()) {
                case "attack":
                case "destroy":
                case "kill":
                    // check if Noun is in the game
                    if (baseRegistry.has(match.get(1).value())) {
                        Entity target = baseRegistry.get(match.get(1).value());
                        return new AttackOrder(target.getLocation(), 10, true);
                    }
                    break;
                case "defend":
                case "protect":
                    if (baseRegistry.has(match.get(1).value())) {
                        return new DefendOrder(baseRegistry.get(match.get(1).value()).getLocation(), 200, 15, 10, true);
                    }
                    break;
            }
            return null;
        }
        return null;
    }

    public String buildTaggedSentence(ArrayList<TaggedWord> words) {
        StringBuilder builder = new StringBuilder();
        for (TaggedWord word : words) builder.append(word + " ");
        return builder.toString();
    }

    /**
     * Gets the first single instance of words that match the input
     *
     * @param words Words to search through
     * @param tags  Tag array to look for in words
     * @return Sublist of the input that contains the matched sequence, null if not found
     */
    public List<TaggedWord> getFirstInstance(ArrayList<TaggedWord> words, String... tags) {
        outerLoop:
        for (int i = 0; i < (words.size() - tags.length) + 1; i++) {
            for (int j = 0; j < tags.length; j++)
                if (!words.get(i + j).tag().equalsIgnoreCase(tags[j])) continue outerLoop;
            // If we reach here then we got a match for every tag in sequence
            return words.subList(i, i + tags.length);
        }
        return null;
    }
}
