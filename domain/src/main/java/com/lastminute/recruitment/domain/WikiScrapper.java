package com.lastminute.recruitment.domain;

import com.lastminute.recruitment.domain.error.WikiPageNotFound;

import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class WikiScrapper {

    private final WikiReader wikiReader;
    private final WikiPageRepository repository;

    public WikiScrapper(WikiReader wikiReader, WikiPageRepository repository) {
        this.wikiReader = wikiReader;
        this.repository = repository;
    }

    public void read(String rootLink) {
        Stack<String> linkStack = new Stack<>();
        Set<String> processedLinks = new TreeSet<>();

        linkStack.push(rootLink);

        while (!linkStack.isEmpty()) {
            String link = linkStack.pop();

            try {
                WikiPage page = wikiReader.read(link);
                repository.save(page);

                processedLinks.add(link);

                page.getLinks().stream()
                        .filter(subPageLink -> !processedLinks.contains(subPageLink))
                        .forEach(linkStack::push);
            } catch (WikiPageNotFound e) {
                System.out.printf("Ignoring not found page %s%n", link);
            }
        }
    }
}
