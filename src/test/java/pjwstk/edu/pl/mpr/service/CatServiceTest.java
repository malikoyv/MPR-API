package pjwstk.edu.pl.mpr.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pjwstk.edu.pl.mpr.exception.CatNotFoundException;
import pjwstk.edu.pl.mpr.model.Cat;
import pjwstk.edu.pl.mpr.repository.CatRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CatServiceTest {
    @Mock
    private PDDocument document;

    @Mock
    private CatRepository repository;

    @InjectMocks
    private CatService service;

    private final Cat cat = new Cat(1L, "KoTkA", 16);

    @Test
    public void getAllCatsSuccess(){
        when(repository.findAll()).thenReturn(List.of(cat));

        List<Cat> cats = service.getAll();

        assertNotNull(cats);
        assertEquals("KoTkA", cats.getFirst().getName());
    }

    @Test
    public void getAllCatsNotFound() {
        when(repository.findAll()).thenReturn(List.of());

        assertThrows(CatNotFoundException.class, () -> service.getAll());
    }

    @Test
    public void getByNameSuccess(){
        when(repository.findByName("KoTkA")).thenReturn(List.of(cat));

        List<Cat> cats = service.getByName("KoTkA");

        assertNotNull(cats);
        assertEquals("KoTkA", cats.getFirst().getName());
    }

    @Test
    public void getByNameNotFound() {
        when(repository.findByName(any())).thenReturn(List.of());

        assertThrows(CatNotFoundException.class, () -> service.getByName(null));
    }

    @Test
    public void addCatSuccess(){
        when(repository.save(cat)).thenReturn(cat);

        Cat newCat = service.addCat(cat);

        assertNotNull(newCat);
        assertNotEquals(0, newCat.getIdentificator());
    }

    @Test
    public void getByAgeSuccess() {
        when(repository.findByAge(16)).thenReturn(List.of(cat));

        List<Cat> cats = service.getByAge(16);

        assertNotNull(cats);
        assertEquals("KoTkA", cats.getFirst().getName());
    }

    @Test
    public void deleteByNameSuccess() {
        when(repository.findByName("KoTkA")).thenReturn(List.of(cat));
        service.deleteByName("KoTkA");

        verify(repository).deleteAll(List.of(cat));
    }

    @Test
    public void changeNameSuccess() {
        when(repository.findByName("KoTkA")).thenReturn(List.of(cat));

        List<Cat> newCats = service.changeName("KoTkA", "kot");

        assertNotNull(newCats);
        assertEquals("kot", newCats.getFirst().getName());
    }


    @Test
    public void addCatWithUpperNameSuccess() {
        when(repository.save(any(Cat.class))).thenReturn(cat);

        Cat returnedCat = service.addCatWithUpperName(cat.getName(), cat.getAge());

        verify(repository).save(any(Cat.class));
        assertEquals(cat.getName().toUpperCase(), returnedCat.getName());
        assertNotEquals(0, returnedCat.getIdentificator());
    }

    @Test
    public void changeAllUpperToLowerByNameSuccess() {
        when(repository.save(any(Cat.class))).thenReturn(cat);
        when(repository.findByName(cat.getName().toUpperCase())).thenReturn(List.of(cat));

        List<Cat> cats = service.changeAllUpperToLowerByName(cat.getName());

        verify(repository).save(any(Cat.class));
        assertNotEquals(0, cats.size());
        assertEquals(cat.getName().toLowerCase(), cats.getFirst().getName());
        assertNotEquals(0, cats.getFirst().getIdentificator());
    }

    @Test
    public void getCatPdfSuccess() throws IOException {
        when(repository.findById(1L)).thenReturn(Optional.of(cat));
//        when(cat.getId()).thenReturn(1L);
//        when(cat.getName()).thenReturn("C");
//        when(cat.getAge()).thenReturn(1);
//        when(cat.getIdentificator()).thenReturn(68L);

        byte[] result = service.getCatPdf(cat.getId());

        assertNotNull(result);
        assertTrue(result.length > 0);

        ArgumentCaptor<PDDocument> captor = ArgumentCaptor.forClass(PDDocument.class);

        verify(repository).findById(1L);
        verify(captor.capture());
        PDPage page = captor.getValue().getPages().get(0);
        PDPageContentStream stream = new PDPageContentStream(captor.getValue(), page);

        assertTrue(stream.toString().contains("Name: C"));
        assertTrue(stream.toString().contains("Age: 1"));
        assertTrue(stream.toString().contains("Identificator: 68"));
        
    }
}
