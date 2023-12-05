package com.lastminute.recruitment.domain;

import com.lastminute.recruitment.domain.error.WikiPageNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
public class WikiScrapperTest {

    private WikiReader wikiReader;
    private WikiPageRepository wikiPageRepository;
    private WikiScrapper wikiScrapper;

    @BeforeEach
    public void setup() {
        wikiReader = mock(WikiReader.class);
        wikiPageRepository = mock(WikiPageRepository.class);

        wikiScrapper = new WikiScrapper(wikiReader, wikiPageRepository);
    }

    @Test
    public void scrapsLinks() {
        //given
        String pageLink = "/";
        String subPageLink = "/subPage";
        String subSubPageLink = "/subSubPage";

        WikiPage page = new WikiPage("page", "content", pageLink, List.of(subPageLink));
        when(wikiReader.read(pageLink)).thenReturn(page);

        WikiPage subPage = new WikiPage("subPage", "content", subPageLink, List.of(subSubPageLink));
        when(wikiReader.read(subPageLink)).thenReturn(subPage);

        WikiPage subSubPage = new WikiPage("subSubPage", "content", subPageLink, emptyList());
        when(wikiReader.read(subSubPageLink)).thenReturn(subSubPage);

        //when
        wikiScrapper.read(pageLink);

        //then
        verify(wikiPageRepository).save(page);
        verify(wikiPageRepository).save(subPage);
        verify(wikiPageRepository).save(subSubPage);
    }

    @Test
    public void handlesWikiPageNotFound() {
        //given
        String pageLink = "/";
        String subPageLink = "/subPage";

        WikiPage page = new WikiPage("page", "content", pageLink, List.of(subPageLink));
        when(wikiReader.read(pageLink)).thenReturn(page);

        when(wikiReader.read(subPageLink)).thenThrow(WikiPageNotFound.class);

        //expect
        assertDoesNotThrow(() -> wikiScrapper.read(pageLink));
    }

    @Test
    public void handlesCycles() {
        //given
        String pageLink = "/";
        String subPageLink = "/subPage";
        String subSubPageLink = "/subSubPage";

        WikiPage page = new WikiPage("page", "content", pageLink, List.of(subPageLink));
        when(wikiReader.read(pageLink)).thenReturn(page);

        WikiPage subPage = new WikiPage("subPage", "content", subPageLink, List.of(subSubPageLink));
        when(wikiReader.read(subPageLink)).thenReturn(subPage);

        WikiPage subSubPage = new WikiPage("subSubPage", "content", subPageLink, List.of(pageLink));
        when(wikiReader.read(subSubPageLink)).thenReturn(subSubPage);

        //when
        wikiScrapper.read(pageLink);

        //then
        verify(wikiPageRepository).save(page);
        verify(wikiPageRepository).save(subPage);
    }
}
